# PolygonBazooka-Server

The backend of [Polygon Bazooka](https://github.com/BlastyTheDev/PolygonBazooka), including the multiplayer servers and leaderboards.

A website for the game is planned.

### Usage

Although the server is intended to only be hosted in one location, you can run it elsewhere.

You will need:
- PostgreSQL database
- JDK 21 or later

1. Download the latest release from the releases page or clone the repository if you want to build it yourself (not recommended)
2. Create a database in PostgreSQL and name it `polygonbazooka`
3. Run the .jar file with a terminal

The server will start on port `8080`. Everything should be working as expected.

You will see that the server creates any missing tables in the database.

If it doesn't work, make sure your database is running, and your user has the correct permissions.
