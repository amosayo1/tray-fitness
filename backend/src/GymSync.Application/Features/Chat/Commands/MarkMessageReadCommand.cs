using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Chat.Commands;

public record MarkMessageReadCommand : IRequest<Result>
{
    public Guid MessageId { get; init; }
}

public class MarkMessageReadCommandHandler : IRequestHandler<MarkMessageReadCommand, Result>
{
    private readonly IApplicationDbContext _context;

    public MarkMessageReadCommandHandler(IApplicationDbContext context)
    {
        _context = context;
    }

    public async Task<Result> Handle(MarkMessageReadCommand request, CancellationToken ct)
    {
        var message = await _context.ChatMessages
            .FirstOrDefaultAsync(m => m.Id == request.MessageId, ct);

        if (message is null)
            return Result.Failure("Message not found");

        message.IsRead = true;
        message.ReadAt = DateTime.UtcNow;
        await _context.SaveChangesAsync(ct);

        return Result.Success();
    }
}
