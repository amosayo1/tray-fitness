using GymSync.Application.Common.Models;
using GymSync.Application.Features.Chat.Commands;
using GymSync.Application.Features.Chat.Queries;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MediatR;

namespace GymSync.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class ChatController : ControllerBase
{
    private readonly IMediator _mediator;

    public ChatController(IMediator mediator) => _mediator = mediator;

    [HttpPost("send")]
    public async Task<ActionResult<Result<MessageResponse>>> Send(SendMessageCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpPost("{messageId}/read")]
    public async Task<ActionResult<Result>> MarkRead(Guid messageId)
    {
        var result = await _mediator.Send(
            new MarkMessageReadCommand { MessageId = messageId });
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpPost("motivation")]
    public async Task<ActionResult<Result>> SendMotivation(SendMotivationCommand command)
    {
        var result = await _mediator.Send(command);
        return result.Succeeded ? Ok(result) : BadRequest(result);
    }

    [HttpGet("messages")]
    public async Task<ActionResult<Result<List<MessageDto>>>> GetMessages(
        [FromQuery] int page = 1, [FromQuery] int pageSize = 50)
    {
        var result = await _mediator.Send(
            new GetMessagesQuery { Page = page, PageSize = pageSize });
        return Ok(result);
    }
}
