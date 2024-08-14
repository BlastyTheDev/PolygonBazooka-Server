using Microsoft.AspNetCore.Mvc;
using PolygonBazooka_Server.Auth;

namespace PolygonBazooka_Server.Controllers;

[Route("api/auth")]
[ApiController]
public class AuthController : ControllerBase
{
    [HttpPost("login")]
    public IActionResult Login([FromBody] LoginRequest request)
    {
        return Ok();
    }
}