        if(localStorage.username != "" && localStorage.password != ""){
           document.getElementById("user").value = localStorage.username;
              document.getElementById("pass").value = localStorage.password;
        }

        var c = document.getElementById("thisCanvas");
        var c2 = document.getElementById("thisCanvas2");
        var ctx = c.getContext('2d');
        var ctx2 = c2.getContext('2d');
        var button = document.getElementById("clearButton");
        var button2 = document.getElementById("contButton");
        var cont = false;
        var backgroundColor = button2.style.background;


        var startPoint = [0,0];
        var endPoint = [0,0];
        button.addEventListener('click',function(){
            clear(ctx);
            clear(ctx2);
            document.getElementById('Linien').innerHTML='';
            firstClick = true;
        },false);
        button2.addEventListener('click',function(){
            switchContMode();
        },false);


        function switchContMode(){
            if(!cont){
            button2.style ="background-color:green";
            cont = true;
            }else{
            button2.style.background = backgroundColor;
            cont = false;
            }
        }
         function getMousePos(canvas, evt) {
            var rect = canvas.getBoundingClientRect();
            return {
              x: Math.round(evt.clientX - rect.left -5),
              y: Math.round(evt.clientY - rect.top-5)
            };
          }
          function clear(context){
            context.clearRect(0, 0, 300, 300);
            context.beginPath();


          }
            document.body.onkeyup = function(e){
                if(e.keyCode == 32){
                    switchContMode();
                }
            }
          firstClick = true;
          lineDone = false;
          var lastClickX = 0;
          var lastClickY = 0;
           c.addEventListener('click', function(evt) {
            var mousePos = getMousePos(c, evt);
            lastClickX = mousePos.x;
            lastClickY= mousePos.y;

            // writeMessage(c, message);
            if(!cont){
            if(firstClick){
                startPoint[0]= mousePos.x;
                startPoint[1]= mousePos.y;
                ctx.moveTo(mousePos.x,mousePos.y);
                ctx2.moveTo(mousePos.x,mousePos.y);
                firstClick = false;

            }else{
                ctx2.lineTo(mousePos.x,mousePos.y);
                clear(ctx);
                ctx2.stroke();
                // ctx.moveTo(endPoint[0],endPoint[1]);
                makeJsonString();
                firstClick = true;
            }
            }else{
                if(firstClick){
                    ctx.moveTo(mousePos.x,mousePos.y);
                    ctx2.moveTo(mousePos.x,mousePos.y);
                    firstClick = false;
                }else{
                    ctx2.lineTo(mousePos.x,mousePos.y);
                    ctx2.stroke();

                }
            }

          }, false);

           c.addEventListener("mousemove",function(evt){
            var mousePos = getMousePos(c, evt);
            document.getElementById("startP").innerHTML ="("+startPoint[0]+","+ startPoint[1]+")";
            document.getElementById("endP").innerHTML ="("+endPoint[0]+","+ endPoint[1]+")";
            if(!firstClick){
                clear(ctx);
                endPoint[0]= mousePos.x;
                endPoint[1]= mousePos.y;
                ctx.moveTo(lastClickX,lastClickY);
                ctx.lineTo(mousePos.x,mousePos.y);
                ctx.stroke();


            }else if(!lineDone){
                startPoint[0]= mousePos.x;
                startPoint[1]= mousePos.y;

            }
           },false);

            var JsonStringArr = [];
           var j = 0;
           function makeJsonStringWith( xCoorA,  yCoorA,  xCoorB,  yCoorB){
                var JsonString = "";
                JsonString ="{'xCoorA':"+xCoorA+",'yCoorA':"+yCoorA+",'xCoorB':"+xCoorB+",'yCoorB':"+yCoorB+"}";
                document.getElementById('Linien').innerHTML+='<li>'+JsonString+'</li>';
                JsonStringArr[j] = JsonString;
                j++;
           }

            function makeJsonString(){
                makeJsonStringWith(startPoint[0],startPoint[1],endPoint[0],endPoint[1]);
            }

            function login(){

                var http = new XMLHttpRequest();
                var url = "/login";
                var username = document.getElementById("user").value;
                var password = document.getElementById("pass").value;

                var params = "username=" + username + "&password=" + password;
                http.open("POST", url, true);

                //Send the proper header information along with the request
                http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");

                http.onreadystatechange = function() {//Call a function when the state changes.
                    if(http.readyState == 4 && http.status == 200) {
                         var response = http.responseText;
                         response = response.replace(/(\r\n|\n|\r)/gm,"");
                         var successText = "{\"success\": \"true\",}";
                         if(response === successText){
                             localStorage.setItem("username", username);
                             localStorage.setItem("password", password);
                         } else {
                             alert("Falscher Benutzername oder falsches Passwort.");
                         }
                    }
                }
                http.send(params);
            }