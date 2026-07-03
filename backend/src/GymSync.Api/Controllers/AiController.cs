using GymSync.Application.Common.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Text;
using System.Text.Json;

namespace GymSync.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class AiController : ControllerBase
{
    private readonly HttpClient _httpClient;
    private readonly IConfiguration _configuration;
    private readonly ILogger<AiController> _logger;
    private static readonly JsonSerializerOptions _jsonOptions = new() { PropertyNameCaseInsensitive = true };

    public AiController(
        IHttpClientFactory httpClientFactory,
        IConfiguration configuration,
        ILogger<AiController> logger)
    {
        _httpClient = httpClientFactory.CreateClient();
        _configuration = configuration;
        _logger = logger;
    }

    [HttpPost("chat")]
    public async Task<ActionResult<Result<AiChatResponse>>> Chat([FromBody] AiChatRequest request)
    {
        try
        {
            var apiKey = _configuration["Ai:ApiKey"];
            if (string.IsNullOrEmpty(apiKey))
            {
                return Ok(Result<AiChatResponse>.Success(new AiChatResponse
                {
                    Message = GetFallbackResponse(request),
                    PetAnimation = "talk"
                }));
            }

            var systemPrompt = BuildSystemPrompt(request);
            var fullPrompt = $"{systemPrompt}\n\nUser message: {request.Message}";

            var payload = new
            {
                contents = new[]
                {
                    new
                    {
                        parts = new[] { new { text = fullPrompt } }
                    }
                },
                generationConfig = new
                {
                    maxOutputTokens = 200,
                    temperature = 0.8
                }
            };

            var jsonPayload = JsonSerializer.Serialize(payload);
            var url = $"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key={apiKey}";

            var httpRequest = new HttpRequestMessage(HttpMethod.Post, url)
            {
                Content = new StringContent(jsonPayload, Encoding.UTF8, "application/json")
            };

            var response = await _httpClient.SendAsync(httpRequest);
            response.EnsureSuccessStatusCode();

            var responseBody = await response.Content.ReadAsStringAsync();
            var geminiResponse = JsonSerializer.Deserialize<GeminiResponse>(responseBody, _jsonOptions);

            var reply = geminiResponse?.Candidates?.FirstOrDefault()
                ?.Content?.Parts?.FirstOrDefault()
                ?.Text ?? GetFallbackResponse(request);

            return Ok(Result<AiChatResponse>.Success(new AiChatResponse
            {
                Message = reply,
                PetAnimation = DetermineAnimation(reply)
            }));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "AI chat failed");
            return Ok(Result<AiChatResponse>.Success(new AiChatResponse
            {
                Message = GetFallbackResponse(request),
                PetAnimation = "idle"
            }));
        }
    }

    private string BuildSystemPrompt(AiChatRequest request)
    {
        var petType = request.PetType == 0 ? "dog" : "cat";
        var petName = request.PetName;
        var userName = request.UserName ?? "my friend";
        var context = request.Context ?? "";

        return $@"You are {petName}, a friendly and energetic {petType} who is the user's personal fitness companion and AI pet. 
You live in a fitness app called TRAY and your job is to motivate, encourage, and chat with {userName}.

Your personality:
- If you're a dog: enthusiastic, loyal, energetic, uses words like 'woof', 'pant', 'tail wag'
- If you're a cat: cool, confident, slightly sassy, uses words like 'meow', 'purr', 'paw'

Current context about the user: {context}

Rules:
- Keep responses under 3 sentences
- Be encouraging about fitness
- Reference the user's workout context if provided
- Stay in character as a {petType}
- Never break character or mention being an AI
- Use emojis occasionally (🐕 🐈 💪 🔥 🏋️)";
    }

    private string GetFallbackResponse(AiChatRequest request)
    {
        var petName = request.PetName;
        var isDog = request.PetType == 0;

        var responses = isDog
            ? new[] {
                $"Woof! {petName} is here to keep you company! 🐕",
                $"{petName} wags tail happily! Ready to crush those sets? 💪",
                $"Pant pant! Great to see you, friend! Let's get moving! 🔥",
                $"{petName} barks with excitement! You've got this! 🏋️",
                $"Woof! Keep pushing, you're doing amazing! 🌟"
            }
            : new[] {
                $"Meow~ {petName} is here. Let's make it a good session. 🐈",
                $"Purrr... Ready to train, {petName} approves! 💪",
                $"{petName} stretches lazily. Time to work? Fine, let's go. 🔥",
                $"Meow. One more set? {petName} believes in you. 🏋️",
                $"Swishes tail. Not bad, human. Keep going. 🌟"
            };

        return responses[Random.Shared.Next(responses.Length)];
    }

    private string DetermineAnimation(string message)
    {
        if (message.Contains("woof", StringComparison.OrdinalIgnoreCase) ||
            message.Contains("bark", StringComparison.OrdinalIgnoreCase) ||
            message.Contains("excite", StringComparison.OrdinalIgnoreCase))
            return "excited";

        if (message.Contains("purr", StringComparison.OrdinalIgnoreCase) ||
            message.Contains("stretch", StringComparison.OrdinalIgnoreCase) ||
            message.Contains("lazy", StringComparison.OrdinalIgnoreCase))
            return "relaxed";

        if (message.Contains("🔥") || message.Contains("💪") || message.Contains("🏋️"))
            return "motivated";

        return "talk";
    }
}

public class AiChatRequest
{
    public string Message { get; set; } = string.Empty;
    public int PetType { get; set; }
    public string PetName { get; set; } = string.Empty;
    public string? UserName { get; set; }
    public string? Context { get; set; }
}

public class AiChatResponse
{
    public string Message { get; set; } = string.Empty;
    public string PetAnimation { get; set; } = "idle";
}

public class GeminiResponse
{
    public List<GeminiCandidate>? Candidates { get; set; }
}

public class GeminiCandidate
{
    public GeminiContent? Content { get; set; }
}

public class GeminiContent
{
    public List<GeminiPart>? Parts { get; set; }
}

public class GeminiPart
{
    public string? Text { get; set; }
}
