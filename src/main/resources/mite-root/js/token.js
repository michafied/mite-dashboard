function setup(){
    document.getElementById("token").addEventListener("keyup", handleInput, true);
}

function handleInput(event) {
    event.preventDefault();
    // Number 13 is the "Enter" key on the keyboard
    if (event.keyCode === 13) {
        sendToken();
    }
}

function sendToken() {
  var title = document.getElementById("title");
  var input = document.getElementById("input");
  var token = document.getElementById("token").value.trim();

  http(() => { input.innerHTML = "<h1>loading</h1>"; })
    .post(
      "/token",
      {
        "token": token
      },
      response => {
        title.innerHTML = "Token saved";
        input.innerHTML = "";
        window.location.href = "/dashboard.html";
      },
      response => {
        input.innerHTML = "<p class=\"error\">"+JSON.parse(response).error+"</p>";
      },
      xhr => {
        input.innerHTML = "<p class=\"error\">error: "+xhr.status+"</p>";
      }
    );
}
