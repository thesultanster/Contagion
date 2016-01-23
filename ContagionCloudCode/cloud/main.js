
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

// addPersonToGame
Parse.Cloud.define("addPersonToGame", function(request, response) {
  
  var userId = request.params.userId
  var Game = Parse.Object.extend("Game");
  var query = new Parse.Query(Game);
      query.equalTo("isWaiting", true);
      query.first({
    success: function(object) {
      // Successfully retrieved the object.
      object.addUnique("players", userId);
      object.save()
    },
    error: function(error) {
      alert("Error: " + error.code + " " + error.message);
    }
  });

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
