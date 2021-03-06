Tractor Beam
============

![beam me up scotty](https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcRRTooGZUc2vm7cLCVxDm6pcecuCQIIvxdY90X9IIf-L9LNYYE4)

Tractor Beam is an android app which will beam up a single page website into a hybrid environment. The first time you open the Tractor Beam application you will need to initialize it with a URL to a single-page web app i.e. web address to index.html. The application will then load the index page as well as all its referenced resources and store them locally. As soon as you have added the URL and clicked on fetch the web app will appear as a full screen app. The next time the app is started it will show the web app.

###### Configuration beam.json
Once a url has been entered into tractor beam it will loook for a ``beam.json`` file in the root of the application. This is where certain settings can be set like provider details.

```json
{
   "name":"my_application_name",
   "version":"1.0.0",
   "maps":[
      {
         "type":"internet",
         "endpoint":"http://api.tiles.mapbox.com/v3/[account].[mapid].json",
         "cacheMode":"on-demand"
      },
      {
         "type":"internet",
         "endpoint":"http://api.tiles.mapbox.com/v3/[account].[mapid].json",
         "cacheMode":"full",
         "extents":[
            {
               "boundingBox":"-85.233,40.0528,-64.4029,48.8575",
               "minZoom":"7",
               "maxZoom":"8"
            },
            {
               "boundingBox":"-14.9644,50.4925,1.3832,57.0228",
               "minZoom":"7",
               "maxZoom":"8"
            }
         ]
      },
      {
         "type":"local",
         "endpoint":"path_to_mbtiles_file_on_sd",
         "geoJsonEndpoint":"path_to_geojson_file_on_sd",
         "cacheMode":"data-only"
      }
   ]
}
```
###### Updating the Andbtiles library
The Anbtiles library can be imported as a project library or as a gradle dependency. Check out the Andbtiles [Quick Start Guide] for details. 

[Quick Start Guide]:https://github.com/tesera/andbtiles/wiki/Quick-Start-Guide
