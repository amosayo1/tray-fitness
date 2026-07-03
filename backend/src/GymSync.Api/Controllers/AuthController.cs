using GymSync.Application.Common.Models;
using GymSync.Application.Features.Auth.Commands;
using MediatR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace GymSync.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly IMediator _mediator;

    public AuthController(IMediator mediator) => _mediator = mediator;

    [HttpPost("login")]
    [AllowAnonymous]
    public async Task<ActionResult<Result<TokenResult>>> Login(LoginCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : Unauthorized(result);
    }

    [HttpPost("activate")]
    [AllowAnonymous]
    public async Task<ActionResult<Result<TokenResult>>> Activate(ActivateAccountCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpPost("refresh")]
    [AllowAnonymous]
    public async Task<ActionResult<Result<TokenResult>>> Refresh(RefreshTokenCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : Unauthorized(result);
    }

    [HttpPost("change-password")]
    [Authorize]
    public async Task<ActionResult<Result>> ChangePassword(ChangePasswordCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpPost("generate-invite")]
    [Authorize]
    public async Task<ActionResult<Result<string>>> GenerateInvite()
    {
        var result = await _mediator.Send(new GenerateInviteTokenCommand());
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }
}
