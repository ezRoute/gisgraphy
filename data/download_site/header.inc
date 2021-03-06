<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <title>Free opensource geocoder and webservices for geonames and openstreetmap data</title>
    <style type="text/css" media="all">
      @import url("/css/dropdown/dropdown.css");
      @import url("/css/dropdown/themes/gisgraphy/default.advanced.css");
      @import url("/css/site.css");
    </style>
            <META NAME="Author" CONTENT="David Masclet"/>
		<META NAME="Keywords" CONTENT="geocoding, webservices, find nearby,geonames,openstreetmap, geolocalisation"/>
		<META NAME="Description" CONTENT="Since 2006, Gisgraphy is a free, open source framework that offers the possibility to do geolocalisation and geocoding via Java APIs or REST webservices. Results can be output in XML, Atom, RSS, JSON, PHP, Ruby, and Python."/>
    	<META http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    	<script type="text/javascript" src="/scripts/jquery-1.6.1.min.js" ></script>
    	
      </head>
      <body>
	<div id="wrapper_main">
		<a href="https://github.com/gisgraphy/gisgraphy" target="gis"><img style="position: absolute; top: 0; right: 0; border: 0;z-index:9999" src="https://camo.githubusercontent.com/652c5b9acfaddf3a9c326fa6bde407b87f7be0f4/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f6f72616e67655f6666373630302e706e67" alt="Fork me on GitHub" data-canonical-src="https://s3.amazonaws.com/github/ribbons/forkme_right_orange_ff7600.png"></a>
		<script type="text/javascript" src="http://www.google.com/coop/cse/brand?form=cse-search-box&amp;lang=en"></script>
	    	<div id="banner">
			<span id="brand">
		     		<a href="https://www.gisgraphy.com"><img src="/images/logos/logo_48.png" alt="Gisgraphy" title="gisgraphy" /><span class="gisgraphy">Gisgraphy</span></a><br/>
		     		<span class="tagline"> Free opensource geocoder and webservices for geonames and openstreetmap data
				</span>
			</span>
			<div style="float:right;">
				<span class="addthis_toolbox addthis_default_style ">
					<a class="addthis_button_facebook_like" fb:like:layout="button_count"></a>
					<a class="addthis_button_preferred_1"></a>
					<a class="addthis_button_preferred_2"></a>
					<a class="addthis_button_preferred_4"></a>
					<a class="addthis_button_compact"></a>
					<a class="addthis_button_google_plusone" ></a>
				</span>
				<script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#pubid=ra-4e8300a9101fc359"></script>
			</div>
			<br/><br/>
			<span id="gsearch">
				<form action="https://www.gisgraphy.com/sitesearch.htm" id="cse-search-box">
				    <input type="hidden" name="cx" value="partner-pub-7203216364107204:d614hs-anxp" />
				    <input type="hidden" name="cof" value="FORID:10" />
				    <input type="text" name="q" id="gsearchinput" size="18" />
				    <input type="submit" name="sa" value="Search" id="gsearchbutton" />
				</form>
	      	</span>
	     		<div class="clear"/></div>
	      	</div>
		<div id="breadcrumbs">
			   <ul id="nav" class="dropdown dropdown-horizontal">
				<li><a href="https://www.gisgraphy.com/index.htm">Home</a></li>
				<li><a href="#" class="dir">Download &nbsp;&#x25BC;&nbsp;</a>
					<ul>
						<li><a href="https://www.gisgraphy.com/download/index.htm">Gisgraphy</a></li>
						<li><a href="https://www.gisgraphy.com/gisgraphoid.htm#download">Gisgraphoid</a></li>
						<li><a href="https://www.gisgraphy.com/download/download_data.htm">Data</a></li>
						<li><a href="https://www.gisgraphy.com/dump.htm">Dumps</a></li>
					</ul>
				</li>
				<li><a href="#" class="dir">Documentation &nbsp;&#x25BC;&nbsp;</a>
					<ul>
						<li><a href="https://www.gisgraphy.com/documentation/index.htm">Documentation</a></li>
						<li><a href="https://www.gisgraphy.com/documentation/quick-start.htm">Quick start</a></li>
						<li><a href="https://www.gisgraphy.com/documentation/user-guide.htm">User guide</a></li>
						<li><a href="https://www.gisgraphy.com/documentation/relevance-and-performance.htm">Relevance & perfs</a>
						<li><a href="https://www.gisgraphy.com/documentation/addressparser.htm">Address parser</a></li>
						<li><a href="https://www.gisgraphy.com/gisgraphoid.htm">Gisgraphoid</a></li>
						<li><a href="https://www.gisgraphy.com/documentation/installation/index.htm">Installation</a></li>
						<li><a href="https://www.gisgraphy.com/forum/"  target="gisgraphyservices">Forum</a></li>
						<li><a href="https://www.gisgraphy.com/faq.html">FAQ</a></li>
						<li><a href="mailto:contact@gisgraphy.com">Contact</a></li>
						<li><a href="https://www.gisgraphy.com/changes-report.html">Changes Report</a></li>
						<li><a href="https://www.gisgraphy.com/license.html">License</a></li>
						<li><a href="https://www.gisgraphy.com/documentation/client-libraries.htm">Client libraries</a></li>
						<li><a href="https://www.gisgraphy.com/documentation/addons.htm">OpenSearch</a></li>
						<li><a href="https://www.gisgraphy.com/screenshots.htm">Screenshots</a></li>
						<li><a href="https://www.gisgraphy.com/glossary.html">Glossary</a></li>
					</ul>
				</li>
				<li><a href="#" class="dir">Community &nbsp;&#x25BC;&nbsp;</a>
					<ul>
						<li><a href="https://www.gisgraphy.com/forum/">Forum</a></li>
						<li><a href="https://www.gisgraphy.com/planet-gisgraphy.htm">Planet Gisgraphy</a></li>
						<li><a href="https://www.gisgraphy.com/aboutme.htm">About me</a></li>
						<li><a href="https://www.gisgraphy.com/what-s-new.htm">What's new</a></li>
						<li><a href="https://www.gisgraphy.com/logos.htm">Logos</a></li>
						<li><a href="https://www.gisgraphy.com/feedback/index.htm">Feedbacks</a></li>
						<li><a href="https://www.gisgraphy.com/benefactors.htm">Donations</a></li>
						<li><a href="https://www.facebook.com/#!/pages/gisgraphy/273250136033585" target="gisgraphy"><img src="/images/externallogos/icon_facebook.gif" alt ="Gisgraphy on Facebook" title="Gisgraphy on Facebook"/> &nbsp;Facebook</a></li>
					</ul>
				</li>
				<li><a href="#" class="dir">Free online webservices &nbsp;&#x25BC;&nbsp;</a>
					<ul>
						<li><a href="https://www.gisgraphy.com/free-access.php" >Free access</a></li>
						<li class="empty"><a href="#" >Geocoding</a></li>
							<li><a href="http://services.gisgraphy.com/public/geocoding.html" target="gisgraphyservices">Geocoding</a></li>
							<li><a href="http://services.gisgraphy.com/public/geocoding_worldwide.html"  target="gisgraphyservices">With autocompletion</a></li>
						<li class="empty"><a href="#" >Reverse Geocoding</a></li>
							<li><a href="http://services.gisgraphy.com/public/reverse_geocoding_worldwide.html"  target="gisgraphyservices">Reverse geocoding</a></li>
						<li class="empty"><a href="#" >Address Parser</a></li>
							<li><a href="http://services.gisgraphy.com/public/addressparser.html"  target="gisgraphyservices">Address parser</a></li>
						<li class="empty"><a href="#" >Street search</a></li>
							<li><a href="http://services.gisgraphy.com/ajaxfulltextsearch.html?placetype=street"  target="gisgraphyservices">By name</a></li>
							<li><a href="http://services.gisgraphy.com/ajaxfulltextsearch.html?placetype=street&advancedSearch=true"  target="gisgraphyservices">By name and GPS</a></li>
							<li><a href="http://services.gisgraphy.com/public/streetSearch.html"  target="gisgraphyservices">By GPS position</a></li>
							<li><a href="http://services.gisgraphy.com/public/geocoding_worldwide.html"  target="gisgraphyservices">With autocopmletion</a></li>
						<li class="empty"><a href="#">Place search</a></li>
							<li><a href="http://services.gisgraphy.com/ajaxfulltextsearch.html"  target="gisgraphyservices">By text</a></li>
							<li><a href="http://services.gisgraphy.com/ajaxfulltextsearch.html?advancedSearch=true"  target="gisgraphyservices">By text and GPS</a></li>
							<li><a href="http://services.gisgraphy.com/fulltextsearch.html"  target="gisgraphyservices">Webservice UI</a></li>
						<li class="empty"><a href="#" >Find nearby</a></li>
							<li><a href="http://services.gisgraphy.com/ajaxgeolocsearch.html"  target="gisgraphyservices">Around a GPS point</a></li>
							<li><a href="http://services.gisgraphy.com/geolocsearch.html"  target="gisgraphyservices">Webservice UI</a></li>
					</ul>
				</li>
				<li><a href="#" class="dir">Developer zone &nbsp;&#x25BC;&nbsp;</a>
					<ul>
					<li><a href="https://www.gisgraphy.com/developer-guide.htm"  >Developer guide</a></li>
						<li><a href="https://www.gisgraphy.com/xref/index.html"  target="gisgraphy">Browse Sources</a></li>
						<li><a href="https://www.gisgraphy.com/source-repository.html">Checkout sources</a></li>
						<li><a href="https://www.gisgraphy.com/issue-tracking.html">Issue tracker</a></li>
						<li><a href="https://www.gisgraphy.com/apidocs/index.html"  target="gisgraphy">Javadocs</a></li>
						<li><a href="https://gisgraphy.free.fr/mavenrepo" target="gisgraphy">Maven Repository</a></li>
						<li><a href="https://www.gisgraphy.com/bench.htm">Benchs</a></li>
						<li><a href="https://www.gisgraphy.com/road-map.htm">Road map</a></li>
						<li><a href="https://www.gisgraphy.com/changes-report.html">Changes Report</a></li>
					</ul>
				</li>
				<li><a href="#" class="dir importantmenu">Premium &nbsp;&#x25BC;&nbsp;</a>
					<ul>
						<li><a href="https://www.gisgraphy.com/premium/index.htm">What's this</a></li>
						<li><a href="https://www.gisgraphy.com/premium/index.htm#installation">Installation</a></li>
						<li><a href="https://www.gisgraphy.com/premium/index.htm#dump">Dump</a></li>
						<li><a href="https://www.gisgraphy.com/premium/index.htm#sla">Premium webservice</a></li>
						<li><a href="https://www.gisgraphy.com/premium/index.htm#developement">Developement</a></li>
						<li><a href="https://www.gisgraphy.com/faq-premium.html">FAQ</a></li>
					</ul>
				</li>
			</ul>
		</div>
		
      <div class="clear">
        <hr/>
      </div>
   

    <div id="bodyColumn">
      <div id="contentBox">
      <!-- end header -->
