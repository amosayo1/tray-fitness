using System.Net;
using System.Text.Json;
using FluentValidation;
using GymSync.Api.Middleware;
using Microsoft.AspNetCore.Http;

namespace GymSync.Api.Tests;

public class ExceptionMiddlewareTests
{
    [Fact]
    public async Task InvokeAsync_NoException_CallsNextDelegate()
    {
        var nextCalled = false;
        var middleware = new ExceptionMiddleware(
            _ => { nextCalled = true; return Task.CompletedTask; },
            new Microsoft.Extensions.Logging.Abstractions.NullLogger<ExceptionMiddleware>());

        var context = new DefaultHttpContext();
        context.Response.Body = new MemoryStream();

        await middleware.InvokeAsync(context);

        Assert.True(nextCalled);
    }

    [Fact]
    public async Task InvokeAsync_NotFoundException_Returns404()
    {
        var middleware = new ExceptionMiddleware(
            _ => throw new NotFoundException("User not found"),
            new Microsoft.Extensions.Logging.Abstractions.NullLogger<ExceptionMiddleware>());

        var context = new DefaultHttpContext();
        context.Response.Body = new MemoryStream();

        await middleware.InvokeAsync(context);

        Assert.Equal((int)HttpStatusCode.NotFound, context.Response.StatusCode);

        context.Response.Body.Seek(0, SeekOrigin.Begin);
        var body = await new StreamReader(context.Response.Body).ReadToEndAsync();
        var json = JsonDocument.Parse(body);
        Assert.Equal("User not found", json.RootElement.GetProperty("error").GetString());
    }

    [Fact]
    public async Task InvokeAsync_ValidationException_Returns400()
    {
        var middleware = new ExceptionMiddleware(
            _ => throw new ValidationException("Validation failed: Username is required; Password must be at least 6 characters"),
            new Microsoft.Extensions.Logging.Abstractions.NullLogger<ExceptionMiddleware>());

        var context = new DefaultHttpContext();
        context.Response.Body = new MemoryStream();

        await middleware.InvokeAsync(context);

        Assert.Equal((int)HttpStatusCode.BadRequest, context.Response.StatusCode);

        context.Response.Body.Seek(0, SeekOrigin.Begin);
        var body = await new StreamReader(context.Response.Body).ReadToEndAsync();
        var json = JsonDocument.Parse(body);
        Assert.Equal("Validation failed", json.RootElement.GetProperty("error").GetString());
    }

    [Fact]
    public async Task InvokeAsync_UnauthorizedAccessException_Returns403()
    {
        var middleware = new ExceptionMiddleware(
            _ => throw new UnauthorizedAccessException(),
            new Microsoft.Extensions.Logging.Abstractions.NullLogger<ExceptionMiddleware>());

        var context = new DefaultHttpContext();
        context.Response.Body = new MemoryStream();

        await middleware.InvokeAsync(context);

        Assert.Equal((int)HttpStatusCode.Forbidden, context.Response.StatusCode);

        context.Response.Body.Seek(0, SeekOrigin.Begin);
        var body = await new StreamReader(context.Response.Body).ReadToEndAsync();
        var json = JsonDocument.Parse(body);
        Assert.Equal("Insufficient permissions", json.RootElement.GetProperty("error").GetString());
    }

    [Fact]
    public async Task InvokeAsync_GenericException_Returns500()
    {
        var middleware = new ExceptionMiddleware(
            _ => throw new InvalidOperationException("Something broke"),
            new Microsoft.Extensions.Logging.Abstractions.NullLogger<ExceptionMiddleware>());

        var context = new DefaultHttpContext();
        context.Response.Body = new MemoryStream();

        await middleware.InvokeAsync(context);

        Assert.Equal((int)HttpStatusCode.InternalServerError, context.Response.StatusCode);

        context.Response.Body.Seek(0, SeekOrigin.Begin);
        var body = await new StreamReader(context.Response.Body).ReadToEndAsync();
        var json = JsonDocument.Parse(body);
        Assert.Equal("An internal server error occurred", json.RootElement.GetProperty("error").GetString());
    }
}
