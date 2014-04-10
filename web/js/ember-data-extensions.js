DS.RESTAdapter.reopen({
	// Configure API location
	namespace: 'api/v1'
});

// REST interface adapter. Lets Ember Data communicate with our API as if the API used a JSON envelope.
App.ApplicationSerializer = DS.RESTSerializer.extend({

	// Create json envelope when receiving
	normalizePayload: function(type, payload) {

		App.ResolveHypermediaLinks(payload);

		// todo: refactor this block out of extensions.
		if (type == 'App.BuildConfig'){
			var uniqueID = (Math.random()+ "").substring(2) + new Date().getTime();
			payload['id'] = uniqueID;
			var payloadJSON = JSON.stringify(payload,null,"\t");
			payload['configStr'] = payloadJSON;
		}

		var o = {};
		o[type.typeKey + 's'] = payload;
		return o;
	},

	// We don't want a json envelope when sending
	serializeIntoHash: function(hash, type, record, options) {
		var root = Ember.String.camelize(type.typeKey);
		var serialized = this.serialize(record, options);
		$.extend(hash, serialized);
		console.log('serializeIntoHash');
		console.log(hash);
	}
});

App.ResolveHypermediaLinks = function(object) {
	if($.isArray(object)) {
		$(object).each(function(index, element) {
			App.ResolveHypermediaLinks(element);
		})
	} else {
		var links = {};
		var linkFound = false;
		for (var property in object) {
			if (object.hasOwnProperty(property)) {
				var urlPostfixIndex = property.indexOf('_url');
				if (urlPostfixIndex > 0 && urlPostfixIndex == property.length - 4) {
					links[property.substring(0, urlPostfixIndex)] = object[property];
					linkFound = true;
				}
			}
		}
		if (linkFound) {
			object.links = links;
		}
	}
}

// Many Array Reload Extension
var get = Ember.get;
DS.PromiseArray.reopen({
	reloadLinks: function() {
		var records = get(this, 'content'),
			store = get(records, 'store'),
			owner = get(records, 'owner'),
			type = get(records, 'type'),
			name = get(records, 'name'),
			resolver = Ember.RSVP.defer();

		var meta = owner.constructor.metaForProperty(name);
		var link = owner._data.links[meta.key];
		store.findHasMany(owner, link, meta, resolver);
	}
});
DS.PromiseObject.reopen({
	reloadLinks: function() {
		console.log("ember-data-extension.js line 78 - TODO: Refresh manifest model");
		/*var content = get(this, 'content'),
			store = get(content, 'store'),
			owner = get(content, 'container'),
			type = get(content, 'type'),
			name = get(content, 'name'),
			resolver = Ember.RSVP.defer();

		relationship = 'App.Package';
		//var link = owner._data.links[meta.key];
		store.findBelongsTo(owner, link, relationship, resolver);*/
	}
});