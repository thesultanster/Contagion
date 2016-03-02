/* addPersonToGame
    input:  gameID  -   game objectid (passed in as a string)
    output: success message
    result: current user is added to the game, and a success string is passed back
*/
Parse.Cloud.define("addPersonToGame", function(request, response) {

  //gets current parse user
  var user = Parse.User.current();
  var gameId  = request.params.gameId;

  var query = new Parse.Query("Game");
  query.equalTo("objectId", gameId);

  query.first({
    success: function(game) {

      // Successfully retrieved the object.

      //Adding user to the game
      if( (game.get("players")).indexOf(user) < 0 ) {
        game.addUnique("players", user);
        game.addUnique("healthyPlayers", user);
        game.increment("healthyCount");

        game.save(null, {
          success: function(game) {
            alert("added user to game list " + game.id + ": " + game.get("name"));

            //set user status to healthy and gameId to Pointer to game
            user.set("status", "healthy");
            user.set("gameId", game);
            user.save(null, {
              success: function(game) {
                alert(user.id + " joined game " + game.id + ": " + game.get("name"));
                response.success("<addPersonToGame> ***Added user to game*** <addPersonToGame>");
              },
              error: function(game, error) {
                alert("Error: " + error.code + " " + error.message);
                response.error("<ERROR> <addPersonToGame> ***Could not create new game*** <addPersonToGame> <ERROR>");
              }
            });
          },
          error: function(game, error) {
            alert("Error: " + error.code + " " + error.message);
            response.error("<ERROR> <addPersonToGame> ***Did not add user to game*** <addPersonToGame> <ERROR>");
          }
        });

      }
    },
    error: function(error) {
      alert("Error: " + error.code + " " + error.message);
      response.error(" <ERROR> <addPersonToGame> ***Did not add player to the game*** <addPersonToGame> <ERROR>");
    }
  });
});

/* addInfected
    input:  gameId - (string) game objectid of the game the user is currently in
    output: success message
    result: current user is put on the Infected list, and a success string is passed back
*/
Parse.Cloud.define("addInfected", function(request, response) {

  //gets current parse user
  var user = Parse.User.current();

  //Only addInfected if the user is not already an infected
  if (user.get("status") != "infected") {

    //get user's game and check if the user is inside the game
    var query = new Parse.Query("Game");
    query.equalTo(user.gameId);
    query.include("safeZones");
    query.containedIn("healthyPlayers",[user]);

    query.first({
      success: function(game) {
        var isInSafeZone = false;
        // Successfully retrieved the object
        
        //check if currentUser in a save zone (geofencing) 
        var userLocation = user.get("location");
        var safeZones = game.get("safeZones");
        if (safeZones && userLocation) {
          for (var i = 0; i < safeZones.length; ++i) {
            var safeZone = safeZones[i];
            var geoPoint = safeZone.get("location");
            var radius = safeZone.get("radius");
            if (geoPoint.milesTo(userLocation) < radius) {
              // user is inside safe zone 
              isInSafeZone = true;
              break;
            }
          }
        }

        if (!isInSafeZone) {
          //decrement game stats
          game.increment("healthyCount", -1);
          game.increment("infectedCount", 1);

          //remove user from healthy players
          game.remove("healthyPlayers", user);
          game.save();

          //set user status to infected
          user.set("status", "infected");
          user.save();

          response.success("<addInfected> ***I became the It*** <addInfected>");
        } else {
          response.error("<addInfected> ***User is in safe zone*** <addInfected>");
        }
      },
      error: function(error) {
        alert("Error: " + error.code + " " + error.message);
        response.error(" <ERROR> <addInfected> ***Could not find game*** <addInfected> <ERROR>");
      }
    });
  }
});


/* leaveGame
    input:  nothing
    output: success message
    result: current user leaves the game, updates the number of healthy and infected
*/
Parse.Cloud.define("leaveGame", function(request,response) {

  //gets current parse user
  var user = Parse.User.current();

  //get user's game and check if the user is inside the game
  var query = new Parse.Query("Game");
  query.equalTo(user.gameId);
  query.containedIn("players",[user]);

  query.first({
    success: function(game) {

      // Successfully retrieved the object

      //check if user is healthy
      if(user.get("status") == "healthy") {
        //if healthy then decrement healthyCount and remove user from healthyPlayers
        game.increment("healthyCount", -1);
        game.remove("healthyPlayers", user)
      }
      else if (user.get("status") == "infected")
        //else decrement infectCount
        game.increment("infectedCount", -1);

      //remove user from the players 
      game.remove("players",user);
      game.save();

      //set the user status to notPlaying set gameId to null
      user.set("status", "notPlaying");
      user.unset("gameId");
      user.save();
      response.success("<leaveGame> ***I left lol*** <leaveGame>");
    },
    error: function(error) {
      alert("Error: " + error.code + " " + error.message);
      response.error(" <ERROR> <leaveGame> ***Could not find game*** <leaveGame> <ERROR>");
    }
  });

});


/* newGame
    input:  gameName  -   game room name (passed in as a string)
    output: success message 
    result: current user is added to the game, and a success string is passed back
*/
Parse.Cloud.define("newGame", function(request, response) {

  if (!request.params.gameName) {
    alert("Error: newGame needs game name");
    response.error(" <ERROR> <newGame> ***Need game room name to create game*** <newGame> <ERROR>");
  }

  var Game = Parse.Object.extend("Game");
  var game = new Game();
  game.set("name", request.params.gameName);
  game.set("players", []);
  game.set("healthyPlayers", []);
  game.set("healthyCount", 0);
  game.set("infectedCount", 0);
  game.set("gameState", 0);
  game.save(null, {
    success: function(game) {
      alert("newGame created " + game.id + ": " + request.params.gameName);

      Parse.Cloud.run('addPersonToGame', { gameId: game.id }, {
        success: function(success) {
          //response.success("<newGame> ***Created new game!\t " + game.id + ": " + request.params.gameName + "*** <newGame>");
          response.success(game);
        },
        error: function(error) {
          alert("Error: " + error.code + " " + error.message);
          response.error("<ERROR> <newGame> ***Could not add current user to new game*** <newGame> <ERROR>");
        }
      });
    },
    error: function(game, error) {
      alert("Error: " + error.code + " " + error.message);
      response.error("<ERROR> <newGame> ***Could not create new game*** <newGame> <ERROR>");
    }
  });

});
