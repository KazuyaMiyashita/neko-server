console.log("chat.js");

var isLogin = false;
var serverAddr = "http://localhost:2200";

window.onload = function() {
  checkCurrentSession();
}

function changeIsLogin(_isLogin) {
  isLogin = _isLogin;
  var loginOrCreateUserDom = document.getElementById("loginOrCreateUser");
  loginOrCreateUserDom.style.display = !isLogin;
  if (isLogin) {
    console.log("ログインしている");
  } else {
    console.log("ログインしていない");
  }
}

function checkCurrentSession() {
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    switch (xhr.readyState) {
      case 0:
        break;
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        var _isLogin = xhr.status == 200
        changeIsLogin(_isLogin);
        if (xhr.status == 500) {
          alert("something wrong!");
        }
    }
  }
  xhr.open("GET", serverAddr + "/auth/session", false);
  xhr.send();
  xhr.abort();
}

function createUser() {
  console.log(document.createUserForm.screenName.value);
  console.log(document.createUserForm.loginName.value);
  console.log(document.createUserForm.password.value);

  var json = null;
  try {
    json = JSON.stringify({
      screenName: document.createUserForm.screenName.value,
      loginName: document.createUserForm.loginName.value,
      password: document.createUserForm.password.value
    });
  } catch {
    console.log("データが揃ってないよん");
    return false;
  }

  console.log(json);

  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    switch (xhr.readyState) {
      case 0:
        break;
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        var _isLogin = xhr.status == 200
        changeIsLogin(_isLogin);
        if (xhr.status == 500) {
          alert("something wrong!");
        }
    }
  }
  xhr.open("POST", serverAddr + "/users", false);
  xhr.send(json);
  xhr.abort();

  return false;
}
