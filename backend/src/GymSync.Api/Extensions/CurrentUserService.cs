using System.Security.Claims;
using GymSync.Application.Common.Interfaces;

namespace GymSync.Api.Extensions;

public class CurrentUserService : ICurrentUserService
{
    private readonly IHttpContextAccessor _httpContextAccessor;

    public CurrentUserService(IHttpContextAccessor httpContextAccessor)
    {
        _httpContextAccessor = httpContextAccessor;
    }

    public Guid UserId
    {
        get
        {
            var claim = _httpContextAccessor.HttpContext?.User?
                .FindFirst(ClaimTypes.NameIdentifier)?.Value;
            return claim is not null && Guid.TryParse(claim, out var id) ? id : Guid.Empty;
        }
    }

    public string? Username =>
        _httpContextAccessor.HttpContext?.User?
            .FindFirst(ClaimTypes.Name)?.Value;

    public bool IsAdmin
    {
        get
        {
            var claim = _httpContextAccessor.HttpContext?.User?
                .FindFirst("isAdmin")?.Value;
            return bool.TryParse(claim, out var isAdmin) && isAdmin;
        }
    }
}
