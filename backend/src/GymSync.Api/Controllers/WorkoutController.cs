using GymSync.Application.Common.Models;
using GymSync.Application.Features.Workouts.Commands;
using GymSync.Application.Features.Workouts.Queries;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MediatR;

namespace GymSync.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class WorkoutController : ControllerBase
{
    private readonly IMediator _mediator;

    public WorkoutController(IMediator mediator) => _mediator = mediator;

    [HttpPost("start")]
    public async Task<ActionResult<Result<WorkoutResponse>>> Start(StartWorkoutCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpGet("{workoutId}")]
    public async Task<ActionResult<Result<WorkoutDetailDto>>> Get(Guid workoutId)
    {
        var result = await _mediator.Send(new GetWorkoutQuery(workoutId));
        return Ok(result);
    }

    [HttpPost("{workoutId}/join")]
    public async Task<ActionResult<Result>> Join(Guid workoutId)
    {
        var result = await _mediator.Send(new JoinWorkoutCommand { WorkoutId = workoutId });
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpPost("{workoutId}/complete-set")]
    public async Task<ActionResult<Result<SetCompletedResponse>>> CompleteSet(
        Guid workoutId, CompleteSetCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpPost("{workoutId}/finish")]
    public async Task<ActionResult<Result<WorkoutSummaryResponse>>> Finish(
        Guid workoutId, FinishWorkoutCommand command)
    {
        command = command with { WorkoutId = workoutId };
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpPost("{workoutId}/rest-timer")]
    public async Task<ActionResult<Result>> StartRestTimer(
        Guid workoutId, StartRestTimerCommand command)
    {
        command = command with { WorkoutId = workoutId };
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpGet("exercises")]
    public async Task<ActionResult<Result<List<ExerciseDto>>>> GetExercises()
    {
        var result = await _mediator.Send(new GetExercisesQuery());
        return Ok(result);
    }

    [HttpPost("{workoutId}/add-exercise")]
    public async Task<ActionResult<Result<WorkoutExerciseDto>>> AddExercise(
        Guid workoutId, AddExerciseToWorkoutCommand command)
    {
        command = command with { WorkoutId = workoutId };
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpGet("templates")]
    public async Task<ActionResult<Result<List<WorkoutTemplateDto>>>> GetTemplates()
    {
        var result = await _mediator.Send(new GetWorkoutTemplatesQuery());
        return Ok(result);
    }
}
