using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using MySqlConnector;
using PolygonBazooka_Server.Auth;

namespace PolygonBazooka_Server.Controllers;

[Route("api/auth")]
[ApiController]
public class AuthController : ControllerBase
{
    private const string SigningKey = "TemporarySigningKeyThatIsntAKey12345678";

    [HttpPost("login")]
    public IActionResult Login([FromBody] LoginRequest request)
    {
        var tokenHandler = new JwtSecurityTokenHandler();
        var key = Encoding.UTF8.GetBytes(SigningKey);

        var claims = new List<Claim>
        {
            new(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
            new(JwtRegisteredClaimNames.Sub, request.Username),
        };

        var tokenDescriptor = new SecurityTokenDescriptor
        {
            Subject = new ClaimsIdentity(claims),
            Expires = DateTime.MaxValue,
            SigningCredentials =
                new SigningCredentials(new SymmetricSecurityKey(key), SecurityAlgorithms.HmacSha256Signature),
        };

        var token = tokenHandler.CreateToken(tokenDescriptor);
        var jwt = tokenHandler.WriteToken(token);

        return Ok(jwt);
    }

    [HttpPost("register")]
    public Task<IActionResult> Register([FromBody] RegisterRequest request,
        [FromServices] MySqlConnection connection)
    {
        connection.Open();

        var checkExistsQuery = "SELECT COUNT(*) FROM `users` WHERE `username` = @Username OR `email` = @Email;";
        var checkExistsCommand = new MySqlCommand(checkExistsQuery, connection);

        checkExistsCommand.Parameters.AddWithValue("@Username", request.Username);
        checkExistsCommand.Parameters.AddWithValue("@Email", request.Email);

        var exists = (int)checkExistsCommand.ExecuteScalar()! > 0;

        if (exists)
            return Task.FromResult<IActionResult>(Conflict("User already exists"));

        var nextUserIdQuery = "SELECT MAX(id) FROM `users`;";
        var nextUserIdCommand = new MySqlCommand(nextUserIdQuery, connection);

        var nextUserId = (int)(nextUserIdCommand.ExecuteScalar() ?? 0);

        var userId = nextUserId + 1;

        var user = new User
        {
            Id = userId,
            Username = request.Username,
            Password = request.Password,
            Email = request.Email,
            Playtime = 0,
            CreatedAt = DateTime.Now,
        };

        var insertQuery =
            "INSERT INTO `users` (`id`, `username`, `password`, `email`, `playtime`, `created_at`) VALUES (@Id, @Username, @Password, @Email, @Playtime, @CreatedAt);";
        var insertCommand = new MySqlCommand(insertQuery, connection);

        insertCommand.Parameters.AddWithValue("@Id", user.Id);
        insertCommand.Parameters.AddWithValue("@Username", user.Username);
        insertCommand.Parameters.AddWithValue("@Password", user.Password);
        insertCommand.Parameters.AddWithValue("@Email", user.Email);
        insertCommand.Parameters.AddWithValue("@Playtime", user.Playtime);
        insertCommand.Parameters.AddWithValue("@CreatedAt", user.CreatedAt);

        return Task.FromResult<IActionResult>(Ok());
    }
}