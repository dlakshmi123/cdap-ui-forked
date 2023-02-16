/*
 * App Controller
 */

define([], function () {

	var Controller = Em.Controller.extend({

		elements: Em.Object.create(),
		__remaining: -1,

		load: function () {

			/*
			 * This is decremented to know when loading is complete.
			 * There are 4 things to load. See __loaded below.
			 */
			this.__remaining = 5;

			this.set('elements.Flow', Em.ArrayProxy.create({content: []}));
			this.set('elements.Batch', Em.ArrayProxy.create({content: []}));
			this.set('elements.Stream', Em.ArrayProxy.create({content: []}));
			this.set('elements.Procedure', Em.ArrayProxy.create({content: []}));
			this.set('elements.Dataset', Em.ArrayProxy.create({content: []}));

			var self = this;
			var model = this.get('model');

			model.trackMetric('/store/bytes/apps/{id}', 'aggregates', 'storage');

			/*
			 * Load Streams
			 */
			this.HTTP.get('rest', 'apps', model.id, 'streams', function (objects) {

				var i = objects.length;
				while (i--) {

					objects[i] = C.Stream.create(objects[i]);

				}
				self.get('elements.Stream').pushObjects(objects);
				self.__loaded();

			});

			/*
			 * Load Flows
			 */
			this.HTTP.get('rest', 'apps', model.id, 'flows', function (objects) {

				var i = objects.length;
				while (i--) {
					objects[i] = C.Flow.create(objects[i]);
				}
				self.get('elements.Flow').pushObjects(objects);
				self.__loaded();

			});

      /*
       * Load Mapreduces
       */
      this.HTTP.get('rest', 'apps', model.id, 'mapreduce', function (objects) {

          var i = objects.length;
          while (i--) {
              objects[i] = C.Batch.create(objects[i]);
          }
          self.get('elements.Batch').pushObjects(objects);
          self.__loaded();

      });

			/*
			 * Load Datasets
			 */
			this.HTTP.get('rest', 'apps', model.id, 'datasets', function (objects) {

				var i = objects.length;
				while (i--) {
					objects[i] = C.Dataset.create(objects[i]);
				}
				self.get('elements.Dataset').pushObjects(objects);
				self.__loaded();

			});

			/*
			 * Load Procedures
			 */
			this.HTTP.get('rest', 'apps', model.id, 'procedures', function (objects) {

				var i = objects.length;
				while (i--) {
					objects[i] = C.Procedure.create(objects[i]);
				}
				self.get('elements.Procedure').pushObjects(objects);
				self.__loaded();

			});

		},

		__loaded: function () {

			if (!(--this.__remaining)) {

				var self = this;
				/*
				 * Give the chart Embeddables 100ms to configure
				 * themselves before updating.
				 */
				setTimeout(function () {
					self.updateStats();
				}, C.EMBEDDABLE_DELAY);

				this.interval = setInterval(function () {
					self.updateStats();
				}, C.POLLING_INTERVAL);

			}

		},

		unload: function () {

			clearInterval(this.interval);
			this.set('elements', Em.Object.create());

		},

		updateStats: function () {

			if (C.currentPath !== 'App') {
				return;
			}

			var self = this, types = ['Flow', 'Batch', 'Stream', 'Procedure', 'Dataset'];

			if (this.get('model')) {

				var i, models = [this.get('model')];
				for (i = 0; i < types.length; i ++) {
					models = models.concat(this.get('elements').get(types[i]).get('content'));
				}

				/*
				 * Hax until we have a pub/sub system for state.
				 */
				i = models.length;
				while (i--) {
					if (typeof models[i].updateState === 'function') {
						models[i].updateState(this.HTTP);
					}
				}
				/*
				 * End hax
				 */

				// Scans models for timeseries metrics and updates them.
				C.Util.updateTimeSeries(models, this.HTTP);

				// Scans models for aggregate metrics and udpates them.
				C.Util.updateAggregates(models, this.HTTP);

			}

		},

		startAllFlows: function (done) {

			var flows = this.get('elements.Flow').content;
			var toSend = flows.length;
			var toReceive = toSend;

			if (!toSend) {

				done();

			} else {

				var requested = false;

				while(toSend--) {

					flows[toSend].set('currentState', 'STARTING');

					C.socket.request('manager', {
						method: 'start',
						params: [flows[toSend].app, flows[toSend].name, -1, 'FLOW']
					}, function (error, response, flow) {

						flow.set('currentState', 'RUNNING');

						if (!--toReceive) {

							done();

						}

					}, flows[toSend]);
				}
			}

		},

		stopAllFlows: function (done) {

			var flows = this.get('elements.Flow').content;
			var toSend = flows.length;
			var toReceive = toSend;

			if (!toSend) {

				done();

			} else {

				var requested = false;

				while(toSend--) {

					if (flows[toSend].get('currentState') !== 'STOPPED') {

						requested = true;

						flows[toSend].set('currentState', 'STOPPING');

						C.socket.request('manager', {
							method: 'stop',
							params: [flows[toSend].app, flows[toSend].name, -1, 'FLOW']
						}, function (error, response, flow) {

							flow.set('currentState', 'STOPPED');

							if (!--toReceive) {

								done();

							}

						}, flows[toSend]);
					}
				}

				if (!requested) {
					done();
				}

			}

		},

		startAllBatches: function (done) {

			var batches = this.get('elements.Batch').content;
			var toSend = batches.length;
			var toReceive = toSend;

			if (!toSend) {

				done();

			} else {

				var requested = false;

				while(toSend--) {

					batches[toSend].set('currentState', 'STARTING');

					C.socket.request('manager', {
						method: 'start',
						params: [batches[toSend].app, batches[toSend].name, -1, 'BATCH']
					}, function (error, response, batch) {

						batch.set('currentState', 'RUNNING');

						if (!--toReceive) {

							done();

						}

					}, batches[toSend]);
				}
			}

		},

		stopAllBatches: function (done) {

			var batches = this.get('elements.Batch').content;
			var toSend = batches.length;
			var toReceive = toSend;

			if (!toSend) {

				done();

			} else {

				var requested = false;

				while(toSend--) {

					if (batches[toSend].get('currentState') !== 'STOPPED') {

						requested = true;

						batches[toSend].set('currentState', 'STOPPING');

						C.socket.request('manager', {
							method: 'stop',
							params: [batches[toSend].app, batches[toSend].name, -1, 'BATCH']
						}, function (error, response, batch) {

							batch.set('currentState', 'STOPPED');

							if (!--toReceive) {

								done();

							}

						}, batches[toSend]);
					}
				}

				if (!requested) {
					done();
				}

			}

		},

		startAllProcedures: function (done) {

			var procedures = this.get('elements.Procedure').content;
			var toSend = procedures.length;
			var toReceive = toSend;

			if (!toSend) {

				done();

			} else {

				var requested = false;

				while(toSend--) {

					procedures[toSend].set('currentState', 'STARTING');

					C.socket.request('manager', {
						method: 'start',
						params: [procedures[toSend].app, procedures[toSend].name, -1, 'FLOW']
					}, function (error, response, flow) {

						flow.set('currentState', 'RUNNING');

						if (!--toReceive) {

							done();

						}

					}, procedures[toSend]);
				}
			}

		},

		stopAllProcedures: function (done) {

			var procedures = this.get('elements.Procedure').content;
			var toSend = procedures.length;
			var toReceive = toSend;

			if (!toSend) {

				done();

			} else {

				var requested = false;

				while(toSend--) {

					if (procedures[toSend].get('currentState') !== 'STOPPED') {

						requested = true;

						procedures[toSend].set('currentState', 'STOPPING');

						C.socket.request('manager', {
							method: 'stop',
							params: [procedures[toSend].app, procedures[toSend].name, -1, 'QUERY']
						}, function (error, response, procedure) {

							procedure.set('currentState', 'STOPPED');

							if (!--toReceive) {

								done();

							}

						}, procedures[toSend]);
					}
				}

				if (!requested) {
					done();
				}

			}

		},

		/*
		 * Application maintenance features
		 */

		"delete": function () {

			var self = this;

			C.Modal.show(
				"Delete Application",
				"Are you sure you would like to delete this Application? This action is not reversible.",
				$.proxy(function (event) {

					var app = this.get('model');

					C.get('far', {
						method: 'remove',
						params: [app.id]
					}, function () {

						self.transitionToRoute('index');

					});

				}, this));

		},

		/*
		 * Application promotion features
		 */

		promotePrompt: function () {

			var view = Em.View.create({
				controller: this,
				model: this.get('model'),
				templateName: 'promote',
				classNames: ['popup-modal', 'popup-full'],
				credentialBinding: 'C.Env.credential'
			});

			view.append();
			this.promoteReload();

		},

		promoteReload: function () {

			this.set('loading', true);

			var self = this;
			self.set('destinations', []);
			self.set('message', null);
			self.set('network', false);

			$.post('/credential', 'apiKey=' + C.Env.get('credential'),
				function (result, status) {

				$.getJSON('/destinations', function (result, status) {

					if (result === 'network') {

						self.set('network', true);

					} else {

						var destinations = [];

						for (var i = 0; i < result.length; i ++) {

							destinations.push({
								id: result[i].vpc_name,
								name: result[i].vpc_label + ' (' + result[i].vpc_name + '.continuuity.net)'
							});

						}

						self.set('destinations', destinations);

					}

					self.set('loading', false);

				});

			});

		}.observes('C.Env.credential'),

		promoteSubmit: function () {

			this.set("pushing", true);
			var model = this.get('model');
			var self = this;

			var destination = self.get('destination');
			if (!destination) {
				return;
			}

			destination += '.continuuity.net';

			this.HTTP.rpc('fabric', 'promote', [model.id, destination, C.Env.get('credential')],
				function (response) {

				if (response.error) {

					self.set('finished', 'Error');
					if (response.error.name) {
						self.set('finishedMessage', response.error.name + ': ' + response.error.message);
					} else {
						self.set('finishedMessage', response.result.message || JSON.stringify(response.error));
					}

				} else {

					self.set('finished', 'Success');
					self.set('finishedMessage', 'Successfully pushed to ' + destination + '.');
					self.set('finishedLink', 'https://' + destination + '/' + window.location.hash);
				}

				self.set("pushing", false);

			});

		}

	});

	Controller.reopenClass({
		type: 'App',
		kind: 'Controller'
	});

	return Controller;

});
