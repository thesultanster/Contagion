/* addPersonToGame
    input: gameID  -   game objectid (passed in as a string)
    output: current user is added to the game, and a success string is passed back
*/
Parse.Cloud.define("addPersonToGame", function(request, response) {

  //gets current parse user
  var user = Parse.User.current();
  var gameId  = request.params.gameId;

  var query = new Parse.Query("Game");
  query.equalTo("objectId", gameId);

  query.first({
    success: function(post) {

      // Successfully retrieved the object.

      //Adding user to the game
      post.addUnique("players", user);
      post.addUnique("healthyPlayers", user);
      post.increment("healthyCount");
      post.save();

      //set user status to healthy and gameId to Pointer to post
      user.set("status", "healthy");
      user.set("gameId", post.toPointer());
      user.save();

      response.success("<addPersonToGame> ***Added Player to the Game!\t " + gameId + "*** <addPersonToGame>");
    },
    error: function(error) {
      alert("Error: " + error.code + " " + error.message);
      response.error(" <ERROR> <addPersonToGame> ***Did not add player to the game*** <addPersonToGame> <ERROR>");
    }
  });
});

/* addInfected
    input: gameId - (string) game objectid of the game the user is currently in
    output: current user is put on the Infected list, and a success string is passed back
*/
Parse.Cloud.define("addInfected", function(request, response) {

  //TODO: check if currentUser in save zones (geofencing) 

  //gets current parse user
  var user = Parse.User.current();

  var query = new Parse.Query("Game");
  query.equalTo(user.gameId);

  query.first({
    success: function(game) {

      // Successfully retrieved the object

      //decrement game healthyCount
      game.increment("healthyCount", -1);
      game.increment("infectedCount");

      //remove user from healthy players
      game.remove("healthyPlayers", user);
      game.save();

      //set user status to infected
      user.set("status", "infected");
      user.save();

      response.success("<addInfected> ***I became the It*** <addInfected>");
    },
    error: function(error) {
      alert("Error: " + error.code + " " + error.message);
      response.error(" <ERROR> <addInfected> ***Could not find game*** <addInfected> <ERROR>");
    }
  });
});


/* leaveGame
    input: nothing
    output: success message
*/
Parse.Cloud.define("leaveGame", function(request,response) {

  //gets current parse user
  var user = Parse.User.current();


  var query = new Parse.Query("Game");
  query.equalTo(user.gameId);

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
