using System.Text;
using GymSync.Api.Extensions;
using GymSync.Api.Middleware;
using GymSync.Application.Common.Interfaces;
using GymSync.Infrastructure.Data;
using GymSync.Infrastructure.Services;
using GymSync.Infrastructure.SignalR;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "GymSync API",
        Version = "v1",
        Description = "Private Workout Companion API"
    });

    options.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Name = "Authorization",
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        In = ParameterLocation.Header,
        Description = "Enter your JWT token"
    });

    options.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            Array.Empty<string>()
        }
    });
});

var supabaseConn = Environment.GetEnvironmentVariable("SUPABASE_CONNECTION_STRING")
    ?? builder.Configuration.GetConnectionString("SupabaseConnection");
if (!string.IsNullOrEmpty(supabaseConn))
{
    builder.Services.AddDbContext<IApplicationDbContext, ApplicationDbContext>(options =>
        options.UseNpgsql(supabaseConn));
}
else
{
    builder.Services.AddDbContext<IApplicationDbContext, ApplicationDbContext>(options =>
        options.UseSqlite(builder.Configuration.GetConnectionString("DefaultConnection")));
}

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(
                Encoding.UTF8.GetBytes(builder.Configuration["Jwt:Secret"]!)),
            ValidateIssuer = true,
            ValidIssuer = builder.Configuration["Jwt:Issuer"],
            ValidateAudience = true,
            ValidAudience = builder.Configuration["Jwt:Audience"],
            ValidateLifetime = true,
            ClockSkew = TimeSpan.Zero
        };

        options.Events = new JwtBearerEvents
        {
            OnMessageReceived = context =>
            {
                var accessToken = context.Request.Query["access_token"];
                var path = context.HttpContext.Request.Path;

                if (!string.IsNullOrEmpty(accessToken) && path.StartsWithSegments("/hubs"))
                {
                    context.Token = accessToken;
                }

                return Task.CompletedTask;
            }
        };
    });

builder.Services.AddAuthorization();

builder.Services.AddSignalR().AddHubOptions<WorkoutHub>(options =>
{
    options.EnableDetailedErrors = builder.Environment.IsDevelopment();
    options.KeepAliveInterval = TimeSpan.FromSeconds(15);
    options.ClientTimeoutInterval = TimeSpan.FromSeconds(30);
});

builder.Services.AddCors(options =>
{
    options.AddPolicy("MobileApp", policy =>
    {
        policy.AllowAnyOrigin()
            .AllowAnyMethod()
            .AllowAnyHeader();
    });

    options.AddPolicy("SignalR", policy =>
    {
        policy.SetIsOriginAllowed(_ => true)
            .AllowAnyMethod()
            .AllowAnyHeader()
            .AllowCredentials();
    });
});

builder.Services.AddMediatR(cfg =>
    cfg.RegisterServicesFromAssembly(typeof(GymSync.Application.Features.Auth.Commands.LoginCommand).Assembly));

builder.Services.AddHttpContextAccessor();
builder.Services.AddScoped<ICurrentUserService, CurrentUserService>();
builder.Services.AddScoped<ITokenService, TokenService>();
builder.Services.AddScoped<IPasswordService, PasswordService>();
builder.Services.AddScoped<INotificationService, NotificationService>();
builder.Services.AddScoped<IFileStorageService, FileStorageService>();

builder.Services.AddHttpClient();
builder.Services.AddHealthChecks();

var app = builder.Build();

app.UseSwagger();
app.UseSwaggerUI();

app.UseMiddleware<ExceptionMiddleware>();
app.UseMiddleware<RequestLoggingMiddleware>();

app.UseCors("MobileApp");

app.UseAuthentication();
app.UseAuthorization();

var apiHtml = """
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><title>GymSync API</title>
<style>body{font-family:system-ui;max-width:800px;margin:40px auto;padding:0 20px;background:#0f0f0f;color:#e0e0e0}h1{color:#6c5ce7}section{margin:24px 0}.endpoint{background:#1a1a2e;border-radius:8px;padding:12px 16px;margin:8px 0;display:flex;align-items:center;gap:8px}.method{font-weight:700;min-width:48px;padding:2px 8px;border-radius:4px;font-size:13px}.get{background:#00b894;color:#000}.post{background:#6c5ce7;color:#fff}.path{font-family:monospace;flex:1}.status{color:#00b894}input,button{font-size:14px;padding:6px 12px;border-radius:6px;border:1px solid #333;background:#2d2d44;color:#e0e0e0;margin:4px}button{background:#6c5ce7;border:none;cursor:pointer}pre{background:#1a1a2e;padding:12px;border-radius:8px;overflow-x:auto;font-size:13px}.hidden{display:none}</style></head>
<body><h1>🏋 GymSync API</h1><p><span class="status">Healthy</span> — Login: <b>admin</b> / <b>admin123</b></p>
<div id="tokenSection"><input id="tokenInput" type="text" placeholder="Paste token here..." style="width:400px"/>
<button onclick="saveToken()">Save Token</button></div>
<div id="endpoints"></div>
<script>
const BASE='http://'+location.host;
let TOKEN='';
let results={};

function saveToken(){TOKEN=document.getElementById('tokenInput').value;alert('Token saved')}

async function call(method,path,body){
  const opts={method,headers:{}};
  if(TOKEN)opts.headers['Authorization']='Bearer '+TOKEN;
  if(body){opts.headers['Content-Type']='application/json';opts.body=JSON.stringify(body)}
  const res=await fetch(BASE+path,opts);
  const text=await res.text();
  try{return JSON.stringify(JSON.parse(text),null,2)}catch{return text}
}

async function test(path,btnId){
  const btn=document.getElementById(btnId);
  btn.disabled=true;btn.textContent='Loading...';
  const [method,apiPath] = path.split(' ');
  let body=null;
  if(method==='POST'){
    const input=document.getElementById('body_'+btnId);
    if(input&&input.value)try{body=JSON.parse(input.value)}catch(e){alert('Invalid JSON: '+e.message);btn.disabled=false;btn.textContent='Test';return}
  }
  const result=await call(method,apiPath,body);
  document.getElementById('result_'+btnId).textContent=result;
  btn.disabled=false;btn.textContent='Test';
}

async function login(){
  const r=await call('POST','/api/auth/login',{username:'admin',password:'admin123'});
  try{const d=JSON.parse(r);TOKEN=d.data.accessToken;document.getElementById('tokenInput').value=TOKEN;alert('Logged in! Token saved.')}catch{e=>alert('Login failed')}
}

const endpoints=[
  {path:'GET /api/auth/login', note:'curl test only (POST with body)'},
  {path:'POST /api/auth/login', body:{username:'admin',password:'admin123'}},
  {path:'POST /api/admin/generate-invite', note:'Requires admin token'},
  {path:'GET /api/workout/exercises', note:'All 27 exercises'},
  {path:'GET /api/workout/templates', note:'5 workout templates'},
  {path:'GET /api/progress/home', note:'Dashboard data'},
  {path:'GET /api/progress/history?months=3', note:'Progress chart data'},
  {path:'POST /api/steps/log', body:{date:'2026-07-02',steps:8543}},
  {path:'GET /api/steps/history?days=7', note:'Step log history'},
  {path:'POST /api/workout/start', body:{workoutTemplateId:'',notes:'Test workout'}},
  {path:'GET /api/chat/messages?page=1&pageSize=20'},
];

const container=document.getElementById('endpoints');
endpoints.forEach((ep,i)=>{
  const [method,apiPath]=ep.path.split(' ');
  const methodClass=method.toLowerCase();
  const div=document.createElement('div');div.className='endpoint';
  div.innerHTML='<span class="method '+methodClass+'">'+method+'</span><span class="path">'+apiPath+'</span>'+
    (ep.note?' <span style="color:#888;font-size:12px">'+ep.note+'</span>':'')+
    '<button id="btn_'+i+'" onclick="test(\''+ep.path+'\',\''+i+'\')">Test</button>'+
    '<div id="bodydiv_'+i+'" class="'+(ep.body?'':'hidden')+'"><input id="body_'+i+'" type="text" value=\''+JSON.stringify(ep.body||{})+'\' style="width:400px;font-family:monospace;font-size:12px" placeholder="JSON body"/></div>';
  container.appendChild(div);
  const pre=document.createElement('pre');pre.id='result_'+i;pre.textContent='Click Test to run';
  container.appendChild(pre);
});
login();
</script></body></html>
""";
app.MapGet("/", async (HttpResponse res) =>
{
    res.ContentType = "text/html; charset=utf-8";
    await res.WriteAsync(apiHtml);
});
app.MapControllers();
app.MapHub<WorkoutHub>("/hubs/workout").RequireCors("SignalR");
app.MapHealthChecks("/health");

{
    var db = app.Services.GetRequiredService<IApplicationDbContext>();
    if (db is ApplicationDbContext ctx)
    {
        await ctx.Database.EnsureCreatedAsync();
    }
    await GymSync.Api.Data.SeedData.InitializeAsync(db);
}

app.Run();
