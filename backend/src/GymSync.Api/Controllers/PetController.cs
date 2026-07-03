using GymSync.Application.Common.Models;
using GymSync.Application.Features.Pets.Commands;
using GymSync.Application.Features.Pets.Queries;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MediatR;

namespace GymSync.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class PetController : ControllerBase
{
    private readonly IMediator _mediator;

    public PetController(IMediator mediator) => _mediator = mediator;

    [HttpPost]
    public async Task<ActionResult<Result<PetDto>>> SetPet(SetPetCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpGet("my")]
    public async Task<ActionResult<Result<PetDto>>> GetMyPet()
    {
        var result = await _mediator.Send(new GetMyPetQuery());
        return Ok(result);
    }
}
