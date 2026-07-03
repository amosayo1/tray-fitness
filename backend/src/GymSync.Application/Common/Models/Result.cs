namespace GymSync.Application.Common.Models;

public class Result
{
    public bool Succeeded { get; protected set; }
    public string[] Errors { get; protected set; } = Array.Empty<string>();

    public static Result Success() => new() { Succeeded = true };
    public static Result Failure(params string[] errors) => new() { Succeeded = false, Errors = errors };

    public static Result<T> Success<T>(T data) => new() { Succeeded = true, Data = data };
    public static Result<T> Failure<T>(params string[] errors) => new() { Succeeded = false, Errors = errors };
}

public class Result<T> : Result
{
    public T? Data { get; set; }
}
