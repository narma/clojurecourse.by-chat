/*
	Prologue 1.2 by HTML5 UP
	html5up.net | @n33co
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
*/

/*********************************************************************************/
/* Settings                                                                      */
/*********************************************************************************/
function initSkell() {
	var prologue_settings = {

		// skelJS (probably don't need to change anything here unless you know what you're doing)
			skelJS: {
				prefix: 'public/css/style',
				resetCSS: true,
				boxModel: 'border',
				useOrientation: true,
				breakpoints: {
					'widest':	{ range: '1881-', hasStyleSheet: false, containers: 1400, grid: { gutters: 40 } },
					'wide':	{ range: '961-1880', containers: 1200, grid: { gutters: 40 } },
					'normal':	{ range: '961-1620', containers: 960, grid: { gutters: 40 } },
					'narrow':	{ range: '961-1320', containers: 'fluid', grid: { gutters: 20 } },
					'narrower':	{ range: '-960', containers: 'fluid', grid: { gutters: 15 } },
					'mobile':	{ range: '-640', containers: 'fluid', lockViewport: true, grid: { gutters: 15, collapse: true } }
				}
			},

		// skelJS Plugins (ditto; don't change unless you know what you're doing)
			skelJSPlugins: {
				panels: {
					panels: {
						sidePanel: {
							breakpoints: 'narrower',
							position: 'left',
							size: 220,
							html: '<div data-action="moveElement" data-args="sidebar"></div></div>'
						}
					},
					overlays: {
						sidePanelToggle: {
							breakpoints: 'narrower',
							position: 'top-left',
							width: '3.5em',
							height: '2.25em',
							html: '<div data-action="togglePanel" data-args="sidePanel" class="toggle"></div>'
						}
					}
				}
			}

	};

/*********************************************************************************/
/* Don't modify beyond this point unless you know what you're doing!             */
/*********************************************************************************/

// Initialize skelJS
	   skel.init(prologue_settings.skelJS, prologue_settings.skelJSPlugins);
}
