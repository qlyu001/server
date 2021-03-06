// The geocoder used to parse user-input addresses
var geocoder;
// The Google Maps object created in this page
var map;
// The draggable rectangle on the map
var rectangle;
var rectangleIsDragged = false;
var features;
var aNorth;   
var aEast;
var aSouth;   
var aWest;  
var $map;
var rWidth;
var rHeight;
var t1;
var t0;
var time1;
var time2;
var countResponse = 0;
var countReceive = 0;
var MinX;
var MaxX;
var MinY;
var MaxY;
var firstLoad;		
//varaible for dealing with the rectangle in the map
var saveResponse;
var config,el,obj,wkt;

function Fly2Destinaiton() {
  var address = document.getElementById("fly-to").value;
  geocoder.geocode({
    'address' : address
  }, function(results, status) {
    if (status == google.maps.GeocoderStatus.OK) {
      var result = results[0];
      var fromatted_address = result.formatted_address;
      $("#fly-to").val(fromatted_address);
      var bounds = result.geometry.bounds;
      map.fitBounds(bounds);
      MoveRectangle(bounds);
    } else {
      alert("Geocode was not successful for the following reason: " + status);
    }
  });
}

// Create the draggable rectangle for the first time and initialize listeners
/*
function CreateRectangle() {
*/
/*
  var bounds = new google.maps.LatLngBounds(
     new google.maps.LatLng(0, 0),
     new google.maps.LatLng(1, 1));
    
  rectangle = new google.maps.Rectangle({
    bounds : bounds,
    fillOpacity : 0.2,
    strokeOpacity : 0.8,
    draggable : true,
    editable : true
  });*/
  /*
  //TODO: draw a polygon with several points and it need to fixed point
  // here I am there is two vector, one vector is for lattitude, another is for longtitude
  	var lat = [40.4250241,34.669359,32.472695,34.307144,36.509636,36.013561];
  	var long = [-124.101562,-120.234375,-114.697266,-114.169922,-117.092285,-116.520996]
  	var triangleCoords = [];
  	for (i = 0; i < lat.length; i++) { 
    	triangleCoords.push( new google.maps.LatLng(lat[i], long[i]));
    	console.log( lat[i], long[i]);
	}*/

  	/*
   	var triangleCoords = [
    new google.maps.LatLng(40.4250241, -124.101562),
    new google.maps.LatLng(34.669359, -120.234375),
    new google.maps.LatLng(32.472695, -114.697266),
    new google.maps.LatLng(34.307144, -114.169922),
    new google.maps.LatLng(36.509636, -117.092285),
    new google.maps.LatLng(36.013561, -116.520996),
    
    
  ];*//*
  	rectangle = new google.maps.Polygon({
    paths: triangleCoords,
    strokeColor: '#FF0000',
    strokeOpacity: 0.8,
    strokeWeight: 2,
    fillColor: '#FF0000',
    fillOpacity: 0.35
  });
  
  google.maps.event.addListener(rectangle, 'mousedown', function() {rectangleIsDragged = true;});
  google.maps.event.addListener(rectangle, 'mouseup', function() {rectangleIsDragged = false;});
  if ($("#results-panel").length > 0)
    google.maps.event.addListener(rectangle, 'bounds_changed', aggregateQuery);
}
*/
// Move the draggable rectangle to a specific location on the map
/*           
function MoveRectangle(bounds) {
  rectangle.setMap(map); // Ensure it is visible if not
  rectangle.setBounds(bounds);
}*/

// TODO This variable should be thread-safe
var processingRequest = false;
var dataRequest = false;
// Process the request by submitting it to the backend server

function aggregateQuery(){
  if (rectangleIsDragged)
    return;
  if (processingRequest)
    return; // Another request already in progress
  if ($("#fromDatePicker").val().length == 0 || $("#toDatePicker").val().length == 0) {
    alert('Please specify start and end date');
    return;
  }
  if (rectangle == null) {
    alert("Please specify a rectangle");
    return;
  }
  processingRequest = true;
  var fromDate = document.getElementById("fromDatePicker").value;
  var toDate = document.getElementById("toDatePicker").value;
  var ne = rectangle.getBounds().getNorthEast();
  var sw = rectangle.getBounds().getSouthWest();
  
  
 
  requestURL = requestURL = "cgi-bin/aggregate_query.cgi?"
                + "min_lat=" + sw.lat() + "&min_lon=" + sw.lng()
                + "&max_lat=" + ne.lat() + "&max_lon=" + ne.lng()
                + "&fromDate=" + fromDate
                + "&toDate=" + toDate ;
  
  
  /////////////////////////////////////////////////////////
  /*original code
  requestURL = "cgi-bin/aggregate_query.cgi?"
                + "min_lat=" + sw.lat() + "&min_lon=" + sw.lng()
                + "&max_lat=" + ne.lat() + "&max_lon=" + ne.lng()
                + "&fromDate=" + fromDate
                + "&toDate=" + toDate;*/
  jQuery.ajax(requestURL, {success : function(data) {
    min = ((parseInt(data.results.min)/50) - 273.15) * 1.8000 + 32.00;
    $("#min").val(min);
    max = ((parseInt(data.results.max)/50) - 273.15) * 1.8000 + 32.00;
    $("#max").val(max);
    sum = data.results.sum;
    count = parseInt(data.results.count);
    average = ((sum/count / 50.0)-273.15)*1.8+32;
    $("#avg").val(average);
    timeInMillis = parseInt(data.stats.totaltime);
    $("#time").val(timeInMillis);
    $("#partitions").val(data.stats["num-of-temporal-partitions"]);
    $("#files").val(data.stats["num-of-trees"]);
  }, complete: function() {
    processingRequest = false;
  }});
}


function generateImage() {
  if ($("#fromDatePicker").val().length == 0) {
    alert('Please specify start date');
    return;
  }
  if (rectangle == null) {
    alert("Please specify a rectangle");
    return;
  }
  var username = $("#userName").val();
  var email = $("#email").val();
  var emailRegexp = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
  if (!emailRegexp.test(email)) {
    alert("Please enter a valid email address to email you the generated files");
    return;
  }
  var fromDate = $("#fromDatePicker").val();
  var ne = rectangle.getBounds().getNorthEast();
  var sw = rectangle.getBounds().getSouthWest();
  var selectedDatasetOption = $("#datasets option:selected");
  var dataset = selectedDatasetOption.attr("id");
  var datasetURL = selectedDatasetOption.val();
  requestURL = "cgi-bin/generate_image.cgi?"
                + "min_lat=" + sw.lat() + "&min_lon=" + sw.lng()
                + "&max_lat=" + ne.lat() + "&max_lon=" + ne.lng()
                + "&fromDate=" + fromDate
                + "&toDate=" + fromDate // For images, from and to date are the same
                + "&dataset=" + dataset
                + "&dataset_url=" + datasetURL
                + "&user_name=" + username
                + "&email=" + email;
  // Send using Ajax
  jQuery.ajax(requestURL, {success: function(response) {
    alert(response);
  }});
}


var placelist= new Array();//changeVector();

function changeVector(){
	
	Ajax3();
	addOption2();
	//nameQuery();
	
}

//reading the data
var Ajax3 = function ()  
{  //var obj=document.getElementById('vector')
   //var name= "countries.shp"
   
  
    $.getJSON ("placelist/boundaries.shp.txt", function (data)  
    { 
      	
        $.each (data, function (i, item)  
        {    for ( placeKey in item){
        	
        		placelist[i]=item[placeKey]
        		 
          		
            }
        	placelist.sort();
		
		
        });
          
    });  

}  

function  addOption2(){setTimeout(function(){ 
	//use id number to find object
	
	document.getElementById("chooseName").innerHTML = "";
	var  obj=document.getElementById( 'chooseName' );
	//add an option
	
	for(i=0;i<placelist.length;i++){
	obj.add( new  Option( placelist[i] , placelist[i] ));
	}
	
},10); 
}


//this function is for display the polygon, more information see this link https://github.com/arthur-e/Wicket/blob/master/doc/index.html


function displayPoly(){
	
	 el = saveResponse;
	 //console.log(el.value);
	 wkt = new Wkt.Wkt();
	 config = this.map.defaults;
   //config.editable = editable;
   wkt.read(el);
   var polyOptions = {
        strokeColor: '#1E90FF',
        strokeOpacity: 0.8,
        strokeWeight: 2,
        fillColor: '#1E90FF',
        fillOpacity: 0.35    
    };
    t0 = performance.now();	
   	obj = wkt.toObject(this.map.defaults); // Make an object
   	t1 = performance.now();
   	console.log("changing it into a wkt object takes " + (t1 - t0) + " milliseconds.")

   	
   	if (Wkt.isArray(obj)) { // Distinguish multigeometries (Arrays) from objects
   	
   		t0 = performance.now();
		  for (i in obj) {
			   if (obj.hasOwnProperty(i) && !Wkt.isArray(obj[i])) {
					   //console.log(obj[i]);
					   obj[i].setMap(map);
					
				  }
          // console.log(this.features);   
   			
		}

		t1 = performance.now();
		console.log("time to parse the polygon at the client side  and add the polygon to the map " + (t1 - t0) + " milliseconds.")

    } else {
    
    	t0 = performance.now();
		  obj.setMap(map); // Add it to the map
      t1 = performance.now();
		  console.log("time to parse the polygon at the client side  and add the polygon to the map" + (t1 - t0) + " milliseconds.")
    }
    
    //console.log(obj);
   
    //map.fitBounds(obj.getBounce());
}
function clearPolygon(){

	if(obj == null){
		  return;
	}	
  if (Wkt.isArray(obj)) { // Distinguish multigeometries (Arrays) from objects
		for (i in obj) {

			if (obj.hasOwnProperty(i) && !Wkt.isArray(obj[i])) {
					//console.log(obj[i]);
					obj[i].setMap(null);
					
			}
				
	  }
  } else {
		obj.setMap(null); // Add it to the map
		
	}
        


}

function fitBound(){
	
	/*
	var x1 = MinX - 0.0003;
	var y1 = MinY - 0.0003;//Was + 1 in Question & Origonal Answer
	var x2 = MaxX + 0.0003;
	var y2 = MaxY + 0.0003;//Was - 1 in Question & Origonal Answer*/
	console.log(MinX);
	console.log(MinY);
	
	var Bounds = new google.maps.LatLngBounds(
			/*
	          new google.maps.LatLng( x1, y1), //sw
	          new google.maps.LatLng( x2, y2) //ne*/
			//in google map lat=y, lng=x, (south. west) (north,east)
			 new google.maps.LatLng( MinY, MinX), //sw
	          new google.maps.LatLng( MaxY, MaxX) //ne
	        );
	map.fitBounds(Bounds);
}

//this is the request function when the user touch the button
/*
function firstQuery(){
	var chooseName = $('#chooseName :selected').val();
	
 	requestURL = requestURL = "cgi-bin/first_query.cgi?"
                + "chooseName=" + chooseName; 
 	
 	 jQuery.ajax(requestURL, {success: function(response) {
 		console.log(response);
 		var obj = JSON.parse(response);
    	
    	//move it to the bound
    	MinX = parseFloat(obj.MinX);
    	MaxX = parseFloat(obj.MaxX);
    	MinY = parseFloat(obj.MinY);
    	MaxY = parseFloat(obj.MaxY);
    	
    	saveResponse= obj.geometry;
    	console.log("I am not ok");
    	fitBound();
    	clearPolygon();     
        displayPoly();
        
    	
     	 
 	  }, complete: function() {processingRequest = false;} });
 	  
 	
 }*/

function clickTrigger(){
	firstLoad = true;
	nameQuery();
	dataQuery();
}              
	
function nameQuery(){
	var start_time = new Date().getTime();
  countResponse = countResponse + 1;
	
     if (processingRequest){
     	return; // Another request already in progress
     }
    
 	processingRequest = true;
	var chooseName = $('#chooseName :selected').val();
    //alert(chooseName);
    aNorth  =   map.getBounds().getNorthEast().lat();   
    aEast   =   map.getBounds().getNorthEast().lng();
    aSouth  =   map.getBounds().getSouthWest().lat();   
    aWest   =   map.getBounds().getSouthWest().lng();  
    
    $map = $('#map');

	mapDim = {
    		height: $map.height(),
    		width: $map.width()
	}
	    

    rWidth =  $map.width();
    rHeight = $map.height();
    
    
    //console.log(countResponse); 
 	requestURL = requestURL = "cgi-bin/name_query.cgi?"
                + "chooseName=" + chooseName 
                + "&aNorth=" + aNorth
                + "&aEast=" + aEast
                + "&aSouth=" + aSouth
                + "&aWest=" + aWest
                + "&rWidth=" + rWidth
                + "&rHeight=" + rHeight
                + "&firstLoad=" + firstLoad
                + "&countResponse=" + countResponse; 
    
   
              
                
    jQuery.ajax(requestURL, {success: function(response) {
    	var obj = JSON.parse(response);
    	//console.log(response);
    	
    	/*
    	//move it to the bound
    	MinX = parseFloat(obj.MinX);
    	MaxX = parseFloat(obj.MaxX);
    	MinY = parseFloat(obj.MinY);
    	MaxY = parseFloat(obj.MaxY);*/
    	
    	saveResponse= obj.geometry;
    	countReceive = parseInt(obj.countReceive);
    	//fitBound();
    	console.log(countReceive);
    	console.log(countResponse); 
    	
    	
    	/*
    	if(countReceive == parseInt(countResponse)){
    	 
    	} else {
      	  console.log("Not Equal");
    	}*/
    	
    		
    	if(firstLoad == true){
    		//console.log("get it");
    		//move it to the bound
    		clearPolygon();
        	MinX = parseFloat(obj.MinX);
        	MaxX = parseFloat(obj.MaxX);
        	MinY = parseFloat(obj.MinY);
        	MaxY = parseFloat(obj.MaxY);
        	fitBound();
        	saveResponse= obj.geometry;
        	displayPoly();
        	firstLoad = false;
    	}
    	else if(saveResponse != '0'){
    		//alert(saveResponse); 
    		if(countReceive == parseInt(countResponse)){
    			console.log("Equal");
    			clearPolygon();
    			time0 = performance.now();     
        		displayPoly();
        		time1 =  performance.now();
        		var request_time = new Date().getTime() - start_time;
        		console.log("total request time" + request_time + " milliseconds.")
        		console.log("front end display time" + (time1 - time0) + " milliseconds.")
    		}
    		
    	}
     // }
    
    	 
	  }, complete: function() {processingRequest = false;} });
	  
	
}


function dataQuery(){

	
     if (dataRequest){
     	return; // Another request already in progress
     }
    
 	dataRequest = true;
	var chooseName = $('#chooseName :selected').val();
   //alert(chooseName);
 	requestURL = requestURL = "cgi-bin/data_query.cgi?"
                + "chooseName=" + chooseName ;
    
    jQuery.ajax(requestURL, {success: function(response) {
    	//console.log(response);
    	var data = JSON.parse(response);
    	//console.log(data.min);
    	min = parseInt(data.min);
        $("#min").val(min);
        max = parseInt(data.max);
        $("#max").val(max);
        sum = parseInt(data.sum);
        count = parseInt(data.count);
        average = sum/count;
        $("#avg").val(average);
        
      
    	
	   
	  }, complete: function() {dataRequest = false;} });
}

function generateVideo() {
  if ($("#fromDatePicker").val().length == 0 || $("#toDatePicker").val().length == 0) {
    alert('Please specify start and end date');
    return;
  }
  if (rectangle == null) {
    alert("Please specify a rectangle");
    return;
  }
  var username = $("#userName").val();
  var email = $("#email").val();
  var emailRegexp = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
  if (!emailRegexp.test(email)) {
    alert("Please enter a valid email address to email you the generated files");
    return;
  }
  var fromDate = $("#fromDatePicker").val();
  var toDate = $("#toDatePicker").val();
  var ne = rectangle.getBounds().getNorthEast();
  var sw = rectangle.getBounds().getSouthWest();
  var selectedDatasetOption = $("#datasets option:selected");
  var dataset = selectedDatasetOption.attr("id");
  var datasetURL = selectedDatasetOption.val();
  var requestURL = "cgi-bin/generate_image.cgi?"
                + "min_lat=" + sw.lat() + "&min_lon=" + sw.lng()
                + "&max_lat=" + ne.lat() + "&max_lon=" + ne.lng()
                + "&fromDate=" + fromDate
                + "&toDate=" + toDate
                + "&dataset=" + dataset
                + "&dataset_url=" + datasetURL
                + "&user_name=" + username
                + "&email=" + email;
  // Send using Ajax
  jQuery.ajax(requestURL, {success: function(response) {
   
    //alert(response);
     app.mapIt()
  }});
}


function formatDate(date) {
  var year = date.getFullYear();
  var month = (date.getMonth() + 1).toString();
  if (month.length == 1)
    month = "0" + month;
  var day = (date.getDate()).toString();
  if (day.length == 1)
    day = "0" + day;
  return month+"/"+day+"/"+year;         
}

var slider;  
var MillisPerDay = 1000 * 60 * 60 * 24; // Milliseconds in one day
var day0 = new Date(2012, 0, 1); // Minimum day supported

var waitImageURL = 'images/wait.gif';
var sessionoutRedirectURL = 'aggregate_query.html';
$(function () {
  // Assign handler to the "fly-to" input
  $('#fly-to').keypress(function(e) {
    if (e.keyCode == 13) {
      Fly2Destinaiton();
    }
  });
  // Initialize global Ajax handler to show the wait icon
  jQuery(document).ajaxStart( function() {
    jQuery("#modal").show();
    jQuery("#fade").show();
  }). ajaxComplete(function() {
    jQuery("#modal").hide();
    jQuery("#fade").hide();
  });
  
  // Initialize date picker for fromDate and toDate
  $("#toDatePicker").datepicker({
    dateFormat : 'mm/dd/yy',
    changeYear : true,
    changeMonth : true,
    yearRange : "2012:2014",
  }).change( function() {
    var day0 = new Date(2012, 0, 1);
    var value1 = (Date.parse($("#fromDatePicker").val()) - day0.getTime()) / MillisPerDay;
    var value2 = (Date.parse($("#toDatePicker").val()) - day0.getTime()) / MillisPerDay;
    slider.slider('values', [value1, value2]);
  });
  
  $("#fromDatePicker").datepicker({
    dateFormat : 'mm/dd/yy',
    changeYear : true,
    changeMonth : true,
    yearRange : "2012:2014",
  }).change( function() {
    var day0 = new Date(2012, 0, 1);
    var value1 = (Date.parse($("#fromDatePicker").val()) - day0.getTime()) / MillisPerDay;
    var value2 = (Date.parse($("#toDatePicker").val()) - day0.getTime()) / MillisPerDay;
    slider.slider('values', [value1, value2]);
  });
  slider = $("#date-range-selector").slider( {
    range: true,
    min: 0,
    max: 365*4+1,
    values: [ 0, 15 ],
    slide: function( event, ui ) {
      var value1 = ui.values[0];
      var value2 = ui.values[1];
      var fromDate = new Date(day0.getTime() + value1 * MillisPerDay);
      var toDate = new Date(day0.getTime() + value2 * MillisPerDay);
      $("#fromDatePicker").val(formatDate(fromDate));
      $("#toDatePicker").val(formatDate(toDate));
    }
  });
  
  // Assign event handler for the image generation button
  $("#GenerateImage").click(generateImage);
  $("#GenerateVideo").click(generateVideo);
  
  // Initialize Google Map
  var element = document.getElementById("map");

  var mapTypeIds = [];
  for (var type in google.maps.MapTypeId) {
    mapTypeIds.push(google.maps.MapTypeId[type]);
  }
  
  /*
  map = new google.maps.Map(element, {
    center : new google.maps.LatLng(39.502506, -98.356131),
    zoom : 5,
    zoomControlOptions: {
                    position: google.maps.ControlPosition.LEFT_TOP,
                    style: google.maps.ZoomControlStyle.SMALL
                }
    mapTypeId : google.maps.MapTypeId.ROADMAP,
    mapTypeControlOptions : {
      mapTypeIds : mapTypeIds
    }
  });*/
  


   map = new google.maps.Map(element, {
    center : new google.maps.LatLng(39.502506, -98.356131),
    zoom : 5,
    mapTypeId : google.maps.MapTypeId.ROADMAP,
    mapTypeControlOptions : {
      mapTypeIds : mapTypeIds
    }
  });
  
   map.drawingManager = new google.maps.drawing.DrawingManager({
                drawingControlOptions: {
                    position: google.maps.ControlPosition.TOP_CENTER,
                    drawingModes: [
                        google.maps.drawing.OverlayType.MARKER,
                        google.maps.drawing.OverlayType.POLYLINE,
                        google.maps.drawing.OverlayType.POLYGON,
                        google.maps.drawing.OverlayType.RECTANGLE
                    ]
                },
                markerOptions: map.defaults,
                polygonOptions: map.defaults,
                polylineOptions: map.defaults,
                rectangleOptions: map.defaults
            });
   map.drawingManager.setMap(map);
  
  /*
  google.maps.event.addListener(map, 'click', function(event) {
    // Move the selection rectangle in the clicked location
    lat = event.latLng.lat();
    lng = event.latLng.lng();
  
    var ne = map.getBounds().getNorthEast();
    var sw = map.getBounds().getSouthWest();
    var width = (ne.lng() - sw.lng()) / 10.0;
    var height = (ne.lat() - sw.lat()) / 10.0;
    bounds = new google.maps.LatLngBounds(
      new google.maps.LatLng(lat - height, lng - width),
      new google.maps.LatLng(lat + height, lng + width));
    
    //MoveRectangle(bounds);
  });*/
  
  // Create and initialize the draggable rectangle on the map
 // CreateRectangle();
  // google.maps.event.addListener(map, "bounds_changed", mapSettleTime); 
   
   
   google.maps.event.addListener(map, 'zoom_changed', function(event) {
   		if(saveResponse != null){
        //alert("Something Found"); 
        nameQuery();
      }
    	
  });


   google.maps.event.addListener(map, 'bounds_changed', function(event) {
      if(saveResponse != null){
        //alert("Something Found"); 
        nameQuery();
      }
      
  });
  
  
  // Initialize the geocoder
  geocoder = new google.maps.Geocoder();
  
  // Add a new map type which shows an overlay of heat map
  // Copied from navigation.js and disabled for now
  /*var refreshFunc = function(event) {
    $("#bdaytime").val("12-"+$("#time-travel").val()+"-2013");
    // Refresh the map
    window.map.mapTypes.set("NASA", new google.maps.ImageMapType({
      getTileUrl: function(coord, zoom) {
         var size = Math.pow(2, zoom);
         // Update x to wrap-around the earth
         var x = (coord.x % size + size) % size;
         var selectedDataset = $("#datasets").find(":selected").val();
         var selectedCountry = $("#country").find(":selected").val();
         var selectedDate = "2013.12."+pad($("#time-travel").val(), 2);
         //return "http://tile.openstreetmap.org/" + zoom + "/" + coord.x + "/" + coord.y + ".png";
         return selectedDataset+selectedDate+"/tile_"+zoom + "_" + x + "-" + coord.y + ".png";
      },
      tileSize: new google.maps.Size(256, 256),
      name: "NASA",
      maxZoom: 9
    }));
  };
  $("#datasets").change(refreshFunc);
  $("#time-travel").change(refreshFunc);*/
});



