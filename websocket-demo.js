//Establish the WebSocket connection and set up event handlers
var hash = document.location.hash.split("/");

// if (hash.length !== 3) {
//     alert("Specify URI with a topic and username. Example http://localhost:8080#/stuff/bob")
// }
var webSocket = new WebSocket("ws://localhost:8088/ws/86ed98c6-b001-48bb-b31e-da638b979c72")
// var webSocket = new WebSocket("ws://localhost:8080/ws/chat/mTopic/myName");
webSocket.onmessage = function (msg) {
    updateChat(msg);
};
webSocket.onclose = function () {
    alert("WebSocket connection closed")
};

//Send message if "Send" is clicked
id("send").addEventListener("click", function () {
    setInterval(generator, 1000);

    function generator() {
        let obj = {value: 10};
        let json = JSON.stringify(obj);
        sendMessage(json);
    }
});

// //Send message if enter is pressed in the input field
// id("message").addEventListener("keypress", function (e) {
//     if (e.keyCode === 13) {
//         sendMessage(e.target.value);
//     }
// });

//Send a message if it's not empty, then clear the input field
function sendMessage(message) {
    if (message !== "") {
        webSocket.send(message);
        id("message").value = "";
    }
}

//Update the chat-panel, and the list of connected users
function updateChat(msg) {
    insert("chat", msg.data);
}

//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", "<p>" + message + "</p>");
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}