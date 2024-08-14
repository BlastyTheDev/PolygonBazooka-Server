using System.ComponentModel.DataAnnotations.Schema;

namespace PolygonBazooka_Server.Auth;

[Table("users")]
public class User
{
    public int Id { get; set; }
    public string Username { get; set; }
    public string Password { get; set; }
    public string Email { get; set; }
    public long Playtime { get; set; }
    public DateTime CreatedAt { get; set; }
}