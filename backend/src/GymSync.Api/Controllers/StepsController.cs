using GymSync.Application.Common.Models;
using GymSync.Application.Features.Steps.Commands;
using GymSync.Application.Features.Steps.Queries;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MediatR;

namespace GymSync.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class StepsController : ControllerBase
{
    private readonly IMediator _mediator;

    public StepsController(IMediator mediator) => _mediator = mediator;

    [HttpPost("log")]
    public async Task<ActionResult<Result<DailyStepLogDto>>> Log(LogStepsCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpGet("history")]
    public async Task<ActionResult<Result<List<DailyStepLogDto>>>> GetHistory(
        [FromQuery] int days = 30)
    {
        var result = await _mediator.Send(new GetDailyStepsQuery { Days = days });
        return Ok(result);
    }
}
