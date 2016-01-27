
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
    input:  user    -   user pointer (assumes taken from getCurrentUser()
            gameID  -   game objectid (passed in as a string)
    output: success should return a yay of some sort
*/
Parse.Cloud.define("addPersonToGame", function(request, response) {

  //assumes request
  var user = request.params.user;
  var gameId  = request.params.gameId;
  var query = new Parse.Query("Game");

  // query.equalTo("gameState", 0);
  query.equalTo("objectId", gameId);

  query.first({
    success: function(post) {
      // Successfully retrieved the object.
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

  // response.success("***addPersonToGame is DONE***");

  // var query = new Parse.Query("Review");
  // query.equalTo("movie", request.params.movie);
  // query.find({
  //   success: function(results) {
  //     var sum = 0;
  //     for (var i = 0; i < results.length; ++i) {
  //       sum += results[i].get("stars");
  //     }
  //     response.success(sum / results.length);
  //   },
  //   error: function() {
  //     response.error("movie lookup failed");
  //   }
});

