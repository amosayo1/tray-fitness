namespace GymSync.Application.Common.Interfaces;

public interface ITokenService
{
    string GenerateAccessToken(Guid userId, string username, bool isAdmin);
    string GenerateRefreshToken();
    string GenerateInviteToken();
    bool ValidateToken(string token);
}
