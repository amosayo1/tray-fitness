using GymSync.Application.Common.Models;
using GymSync.Application.Features.Stats.Queries;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MediatR;

namespace GymSync.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class ProgressController : ControllerBase
{
    private readonly IMediator _mediator;

    public ProgressController(IMediator mediator) => _mediator = mediator;

    [HttpGet("home")]
    public async Task<ActionResult<Result<HomeDataResponse>>> GetHomeData()
    {
        var result = await _mediator.Send(new GetHomeDataQuery());
        return Ok(result);
    }

    [HttpGet("history")]
    public async Task<ActionResult<Result<ProgressResponse>>> GetProgress(
        [FromQuery] int months = 3)
    {
        var result = await _mediator.Send(
            new GetProgressQuery { Months = months });
        return Ok(result);
    }
}
