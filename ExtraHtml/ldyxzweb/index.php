<!DOCTYPE html>
<?php
	include_once("setchannel.php");
	store($_GET);
?>
<html lang="en">

	<head>

		<meta charset="utf-8">

		<meta name="viewport" content="width=640, user-scalable=no, target-densitydpi=320" />

    <title>乱斗英雄传</title>

     <link rel="stylesheet" href="ldyxz_files/jquery.bxslider.css" />

     <link rel="stylesheet" href="ldyxz_files/main.css" />

     <style>

     	.bx-wrapper .bx-prev {

				left: 10px;

				background: url(ldyxz_files/controls.png) no-repeat 0 0;

		}

			

		.bx-wrapper .bx-next {

			right: 10px;

			background: url(ldyxz_files/controls.png) no-repeat -48px 0;

		}

			

     </style>

     
     <script src="jquery.min.js"></script>
     <script>
		$(document).ready(function() {
			var u = navigator.userAgent;
			//var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Adr') > -1; //android终端
			var isiOS = u.indexOf('iPhone') > -1 || u.indexOf('Mac') > -1 || u.indexOf('iPad') > -1; //ios终端
			if(isiOS) {
				$("a").attr("href", "https://itunes.apple.com/us/app/%E4%B9%B1%E6%96%97%E8%8B%B1%E9%9B%84%E4%BC%A0-%E5%83%8F%E7%B4%A0%E6%8C%82%E6%9C%BA%E5%84%BF%E6%97%B6%E5%9B%9E%E5%BF%86/id1256389992?l=zh&ls=1&mt=8");
			}else {
				$("a").attr("href", "http://download.kalazhu.cn/download/ldyxz/ldyxz_0728.apk");
			}
		});
     </script>

</head>

<body>

	<div class="hover" style="background: url(ldyxz_files/hover.png) no-repeat;">

		<a href="" ><img src="ldyxz_files/download.png" class="down1"></a>

	</div>

    <div class="page">

    	<div class="top">

    		<img src="ldyxz_files/top.jpg">

    		<img src="ldyxz_files/star.gif" class="star">

    		<a href=""  class="freedown"><img src="ldyxz_files/freedown.png"></a>

    	</div>

    	<div class="main" style="background: url(ldyxz_files/main.jpg) no-repeat;">

    		<ul class="bxslider">

			  <li><img src="ldyxz_files/bigking1.jpg"></li>

			  <li><img src="ldyxz_files/bigking2.jpg"></li>

			  <li><img src="ldyxz_files/bigking3.jpg"></li>

			  <li><img src="ldyxz_files/bigking4.jpg"></li>

			  <li><img src="ldyxz_files/bigking5.jpg"></li>

			</ul>

    		<a href=""><img src="ldyxz_files/start.png" class="start"></a>

    		

    	</div>

    	<div class="footer" style="padding:30px 0 30px 0;">

    			

    			上海渡维电子科技有限公司  <br> <br>

				上海市长宁区长宁路855号亨通国际大厦19楼C座    <br><br>

				电话：<span id="tel">021-62258848</span><br><br>  

沪网文(2016) 2156-101号丨沪ICP备13007064号-1

    	</div>

	</div>

    <script type="text/javascript" src="ldyxz_files/jquery1.8.2.min.js" ></script>

    <script type="text/javascript" src="ldyxz_files/jquery.bxslider.js" ></script>

    <script>

    	$('.bxslider').bxSlider({

			  buildPager: function(slideIndex){

			    switch(slideIndex){

			      case 0:

			        return '<img src="ldyxz_files/king1.png">';

			      case 1:

			        return '<img src="ldyxz_files/king2.png">';

			      case 2:

			        return '<img src="ldyxz_files/king3.png">';

			      case 3:

			        return '<img src="ldyxz_files/king4.png">';

			      case 4:

			        return '<img src="ldyxz_files/king5.png">';  

			    }

			  }

			});

    </script>

</body>

</html>

