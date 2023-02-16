/*
 * Flowlet Controller
 */

define([], function () {

	var Controller = Ember.Controller.extend({

		/*
		 * This syntax makes the FlowStatus controller available to this controller.
		 * This allows us to access the flow model that has already been loaded.
		 */
		needs: ['FlowStatus'],

		load: function () {

			/*
			 * The FlowStatus controller has already loaded the flow.
			 * The flow that has been loaded has the flowlet model we need.
			 */
			var flow = this.get('controllers').get('FlowStatus').get('model');
			var model = this.get('model');

			for (var i = 0; i < flow.flowlets.length; i ++) {

				if (flow.flowlets[i].name === model.name) {
					this.set('model', C.Flowlet.create(flow.flowlets[i]));
					break;
				}

			}

			/*
			 * Setup connections based on the Flow.
			 */
			var cx = flow.connections;
			function findContributors(direction, flowlet, input) {
				var res = [];
				var opp = 'from';
				if (direction === 'from') {
					opp = 'to';
				}
				for (var i = 0; i < cx.length; i ++) {
					if (cx[i][direction]['flowlet'] === flowlet &&
						cx[i][direction]['stream'] === input) {
						res.push({name: cx[i][opp]['flowlet'] || cx[i][opp]['stream']});
					}
				}
				return res;
			}

			var streams = flow.flowletStreams[model.name],
				inputs = [], outputs = [];

			/*
			 * Find inputs and outputs.
			 */
			for (var i in streams) {
				if (streams[i].second === 'IN') {
					inputs.push({
						'name': i,
						'contrib': findContributors('to', model.name, i)
					});
				} else if (streams[i].second === 'OUT') {
					outputs.push({
						'name': i,
						'contrib': findContributors('from', model.name, i)
					});
				}
			}
			this.get('model').set('inputs', inputs);
			this.get('model').set('outputs', outputs);

			/*
			 * Select the Inputs tab.
			 */
			this.select('inputs');

			/*
			 * Give the chart Embeddables 100ms to configure
			 * themselves before updating.
			 */
			var self = this;
			setTimeout(function () {
				self.getStats();
			}, C.EMBEDDABLE_DELAY);

		},

		unload: function () {
			clearTimeout(this.__timeout);
		},

		getStats: function (self) {

			var flow = this.get('controllers').get('FlowStatus').get('model');

			C.get.apply(this.get('model'), this.get('model').getUpdateRequest(flow));

			var self = this;

			self.__timeout = setTimeout(function () {
				self.getStats(self);
			}, C.POLLING_INTERVAL);

		},

		select: function (event) {

			var tabName;

			if (typeof event === 'string') {
				tabName = event;
			} else {
				tabName = $(event.target).attr('tab-name');
			}

			$('#flowlet-popup-inputs').hide();
			$('#flowlet-popup-inputs-tab').removeClass('tab-selected');
			$('#flowlet-popup-processed').hide();
			$('#flowlet-popup-processed-tab').removeClass('tab-selected');
			$('#flowlet-popup-outputs').hide();
			$('#flowlet-popup-outputs-tab').removeClass('tab-selected');

			$('#flowlet-popup-' + tabName).show();
			$('#flowlet-popup-' + tabName + '-tab').addClass('tab-selected');

		},
		close: function () {

			var model = this.get('controllers').get('FlowStatus').get('model');

			/*
			 * HAX: The URL route needs the ID of a flow to be app_id:flow_id.
			 * However, Ember is smart enough to not reload the parent controller.
			 * Therefore, the "correct" ID is preserved on the parent controller's model.
			 */

			if (model.id && model.id.indexOf(':') === -1) {
				model.id = model.app + ':' + model.id;
			}

			this.transitionToRoute('FlowStatus', model);

		},
		navigate: function (event) {

			// TODO

		},
		addOneInstance: function () {
			this.confirm('Add 1 instance to ', +1);
		},
		removeOneInstance: function () {

			if (this.get('model').get('instances') > 1) {
				this.confirm('Remove 1 instance from ', -1);
			} else {

				C.Modal.show(
					"Instances Error",
					'Sorry, this Flowlet is only running one instance and cannot be reduced.'
				);

			}

		},
		confirm: function (message, value) {

			var model = this.get('model');
			var name = model.name;
			var self = this;

			C.Modal.show(
				"Flowlet Instances",
				message + '"' + name + '" flowlet?',
				function () {
					self.addInstances(value, function () {

					});
				});

		},
		addInstances: function (value, done) {

			var flow = this.get('controllers').get('FlowStatus').get('model');
			var model = this.get('model');

			var instances = model.get('instances') + value;

			if (instances < 1 || instances > 64) {
				done('Cannot set instances. Please select an instance count > 1 and <= 64');
			} else {

				var app = flow.get('app');
				var flow = flow.get('name');
				var version = flow.version;
				var flowlet = model.name;

				C.get('manager', {
					method: 'setInstances',
					params: [app, flow, version, flowlet, instances]
				}, function (error, response) {

					/*
					 * THIS should be moved out of here. Send in an error callback.
					 */
					if (error) {
						C.Modal.show('Container Error', error);
					} else {
						model.set('instances', instances);
					}

				});

			}
		}

	});

	Controller.reopenClass({
		type: 'FlowStatusFlowlet',
		kind: 'Controller'
	});

	return Controller;

});