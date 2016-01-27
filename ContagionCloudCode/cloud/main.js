
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:

// // add new users to the waiting room 
// Parse.Cloud.beforeSave(Parse.User, function(request, response) {

//   if (request.object.isNew()) {
//     var newUserId = request.object.id;
//     var Game = Parse.Object.extend("Game");
//     var query = new Parse.Query(Game);
//         query.equalTo("isWaiting", true);
//         query.first({
//       success: function(object) {
//         // Successfully retrieved the object.
//         object.addUnique("players", newUserId);
//         object.save()
//       },
//       error: function(error) {
//         alert("Error: " + error.code + " " + error.message);
//       }
//     });
//   }

// });

/* addPersonToGame
    input: gameID  -   game objectid (passed in as a string)
    output: current user is added to the game, and a success string is passed back
*/
Parse.Cloud.define("addPersonToGame", function(request, response) {

  //gets currnt parse user
  var user = Parse.User.current();
  var gameId  = request.params.gameId;
  var query = new Parse.Query("Game");

  // query.equalTo("gameState", 0);
  query.equalTo("objectId", gameId);

  query.first({
    success: function(post) {

      // Successfully retrieved the object.

      //Adding user to the game
      post.addUnique("players", user);
      post.addUnique("healthyPlayers", user);
      post.increment("healthyCount");
      post.save();

      response.success("***Added Player to the Game!***\t " + post.get("objectId"));
    },
    error: function(error) {
      alert("Error: " + error.code + " " + error.message);
      response.error("***Did not add player to the game*** \t");
    }
  });
});

