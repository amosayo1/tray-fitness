using GymSync.Application.Common.Interfaces;
using GymSync.Application.Features.Auth.Commands;
using GymSync.Domain.Entities;
using GymSync.Domain.Enums;
using GymSync.Infrastructure.Data;
using GymSync.Infrastructure.Services;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;

namespace GymSync.Api.Tests;

public class AuthFlowTests : IDisposable
{
    private readonly ApplicationDbContext _context;
    private readonly TokenService _tokenService;
    private readonly PasswordService _passwordService;
    private readonly LoginCommandHandler _loginHandler;
    private readonly RefreshTokenCommandHandler _refreshHandler;
    private readonly User _testUser;
    private readonly User _seedUser;

    public AuthFlowTests()
    {
        var options = new DbContextOptionsBuilder<ApplicationDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;

        _context = new ApplicationDbContext(options);

        var configData = new Dictionary<string, string?>
        {
            { "Jwt:Secret", "ThisIsASecureKeyThatIsLongEnoughForHmacSha256!" },
            { "Jwt:Issuer", "GymSync" },
            { "Jwt:Audience", "GymSyncMobile" },
            { "Jwt:ExpiryMinutes", "60" }
        };
        var configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(configData)
            .Build();

        _passwordService = new PasswordService();
        _tokenService = new TokenService(configuration);

        _loginHandler = new LoginCommandHandler(_context, _passwordService, _tokenService);
        _refreshHandler = new RefreshTokenCommandHandler(_context, _tokenService);

        var passwordHash = _passwordService.HashPassword("testpass123");
        _seedUser = new User
        {
            Id = Guid.NewGuid(),
            Username = "testuser",
            DisplayName = "Test User",
            Email = "test@gymsync.app",
            PasswordHash = passwordHash,
            Status = UserStatus.Offline,
            IsAdmin = false,
            CurrentStreak = 0,
            LongestStreak = 0,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };
        _context.Users.Add(_seedUser);
        _context.SaveChanges();

        _testUser = _seedUser;
    }

    [Fact]
    public async Task Login_WithValidCredentials_ReturnsTokenResult()
    {
        var command = new LoginCommand { Username = "testuser", Password = "testpass123" };
        var result = await _loginHandler.Handle(command, CancellationToken.None);

        Assert.True(result.Succeeded);
        Assert.NotNull(result.Data);
        Assert.NotEmpty(result.Data!.AccessToken);
        Assert.NotEmpty(result.Data.RefreshToken);
    }

    [Fact]
    public async Task Login_WithInvalidPassword_ReturnsFailure()
    {
        var command = new LoginCommand { Username = "testuser", Password = "wrongpassword" };
        var result = await _loginHandler.Handle(command, CancellationToken.None);

        Assert.False(result.Succeeded);
        Assert.NotEmpty(result.Errors);
    }

    [Fact]
    public async Task Login_WithNonExistentUser_ReturnsFailure()
    {
        var command = new LoginCommand { Username = "nonexistent", Password = "testpass123" };
        var result = await _loginHandler.Handle(command, CancellationToken.None);

        Assert.False(result.Succeeded);
        Assert.NotEmpty(result.Errors);
    }

    [Fact]
    public async Task Login_UpdatesUserStatusToOnline()
    {
        var command = new LoginCommand { Username = "testuser", Password = "testpass123" };
        await _loginHandler.Handle(command, CancellationToken.None);

        var user = await _context.Users.FirstAsync(u => u.Username == "testuser");
        Assert.Equal(UserStatus.Online, user.Status);
    }

    [Fact]
    public async Task Login_StoresRefreshToken()
    {
        var command = new LoginCommand { Username = "testuser", Password = "testpass123" };
        await _loginHandler.Handle(command, CancellationToken.None);

        var storedToken = await _context.RefreshTokens.FirstOrDefaultAsync();
        Assert.NotNull(storedToken);
        Assert.Equal(_testUser.Id, storedToken!.UserId);
        Assert.False(storedToken.IsRevoked);
    }

    [Fact]
    public async Task RefreshToken_WithValidToken_ReturnsNewTokens()
    {
        var loginCmd = new LoginCommand { Username = "testuser", Password = "testpass123" };
        var loginResult = await _loginHandler.Handle(loginCmd, CancellationToken.None);
        var oldRefreshToken = loginResult.Data!.RefreshToken;

        var refreshCmd = new RefreshTokenCommand { RefreshToken = oldRefreshToken };
        var refreshResult = await _refreshHandler.Handle(refreshCmd, CancellationToken.None);

        Assert.True(refreshResult.Succeeded);
        Assert.NotNull(refreshResult.Data);
        Assert.NotEqual(oldRefreshToken, refreshResult.Data!.RefreshToken);
        Assert.NotNull(refreshResult.Data.AccessToken);
        Assert.NotEmpty(refreshResult.Data.AccessToken);
    }

    [Fact]
    public async Task RefreshToken_WithInvalidToken_ReturnsFailure()
    {
        var command = new RefreshTokenCommand { RefreshToken = "invalid-token" };
        var result = await _refreshHandler.Handle(command, CancellationToken.None);

        Assert.False(result.Succeeded);
        Assert.NotEmpty(result.Errors);
    }

    [Fact]
    public async Task RefreshToken_RevokesOldToken()
    {
        var loginCmd = new LoginCommand { Username = "testuser", Password = "testpass123" };
        var loginResult = await _loginHandler.Handle(loginCmd, CancellationToken.None);
        var oldTokenValue = loginResult.Data!.RefreshToken;

        var refreshCmd = new RefreshTokenCommand { RefreshToken = oldTokenValue };
        await _refreshHandler.Handle(refreshCmd, CancellationToken.None);

        var oldToken = await _context.RefreshTokens
            .FirstOrDefaultAsync(t => t.Token == oldTokenValue);
        Assert.NotNull(oldToken);
        Assert.True(oldToken!.IsRevoked);
        Assert.NotNull(oldToken.RevokedAt);
    }

    [Fact]
    public async Task RefreshToken_CannotBeReusedAfterRevocation()
    {
        var loginCmd = new LoginCommand { Username = "testuser", Password = "testpass123" };
        var loginResult = await _loginHandler.Handle(loginCmd, CancellationToken.None);
        var oldTokenValue = loginResult.Data!.RefreshToken;

        var refreshCmd = new RefreshTokenCommand { RefreshToken = oldTokenValue };
        await _refreshHandler.Handle(refreshCmd, CancellationToken.None);

        var reuseResult = await _refreshHandler.Handle(refreshCmd, CancellationToken.None);
        Assert.False(reuseResult.Succeeded);
    }

    [Fact]
    public async Task Login_GeneratesValidJwtToken()
    {
        var command = new LoginCommand { Username = "testuser", Password = "testpass123" };
        var result = await _loginHandler.Handle(command, CancellationToken.None);

        var isValid = _tokenService.ValidateToken(result.Data!.AccessToken);
        Assert.True(isValid);
    }

    public void Dispose()
    {
        _context.Dispose();
    }
}
