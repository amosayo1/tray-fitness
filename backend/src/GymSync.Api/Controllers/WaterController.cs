using GymSync.Application.Common.Models;
using GymSync.Application.Features.Water.Commands;
using GymSync.Application.Features.Water.Queries;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MediatR;

namespace GymSync.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class WaterController : ControllerBase
{
    private readonly IMediator _mediator;

    public WaterController(IMediator mediator) => _mediator = mediator;

    [HttpPost("reminder")]
    public async Task<ActionResult<Result<WaterReminderDto>>> SetReminder(
        SetWaterReminderCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpPost("log")]
    public async Task<ActionResult<Result<WaterIntakeDto>>> Log(LogWaterIntakeCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpGet("intakes")]
    public async Task<ActionResult<Result<List<WaterIntakeDto>>>> GetIntakes(
        [FromQuery] Guid? workoutId, [FromQuery] int lastHours = 24)
    {
        var result = await _mediator.Send(
            new GetWaterIntakesQuery { WorkoutId = workoutId, LastHours = lastHours });
        return Ok(result);
    }
}
