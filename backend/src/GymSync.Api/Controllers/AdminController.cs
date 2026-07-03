using GymSync.Application.Common.Models;
using GymSync.Application.Features.Admin.Commands;
using GymSync.Application.Features.Auth.Commands;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MediatR;

namespace GymSync.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(Policy = "AdminOnly")]
public class AdminController : ControllerBase
{
    private readonly IMediator _mediator;

    public AdminController(IMediator mediator) => _mediator = mediator;

    [HttpPost("generate-invite")]
    public async Task<ActionResult<Result<string>>> GenerateInvite()
    {
        var result = await _mediator.Send(new GenerateInviteTokenCommand());
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpPost("deactivate/{userId}")]
    public async Task<ActionResult<Result>> DeactivateAccount(Guid userId)
    {
        var result = await _mediator.Send(
            new DeactivateAccountCommand { UserId = userId });
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpPost("reset-password")]
    public async Task<ActionResult<Result<string>>> ResetPassword(ResetUserPasswordCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }
}
