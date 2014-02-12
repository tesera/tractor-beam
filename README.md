Tractor Beam
============

![beam me up scotty](https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcRRTooGZUc2vm7cLCVxDm6pcecuCQIIvxdY90X9IIf-L9LNYYE4)

Tractor Beam is an android app which will beam up a single page website into a hybrid environment. The first time you open the Tractor Beam application you will need to initialize it with a URL to a single-page web app i.e. web address to index.html. The application will then load the index page as well as all its referenced resources and store them locally. As soon as you have added the URL and clicked on fetch the web app will appear as a full screen app. The next time the app is started it will show the web app.

######Configuration tractor-beam-config.json
Once a url has been entered into tractor beam it will loook for a ``tractor-beam-config.json`` file in the root of the application. This is where certain settings can be set like provider details.

````
{
	name: "my application name"
	version: "1.0.0",
	providers: {
		andbtiles: {
			maps: [
				{
					type: 'internet',
					endpoint: 'http://api.tiles.mapbox.com/v3/[account].[mapid].json',
					cacheMode: 'on-demand'
				}
			]
		}
	}
}
````
