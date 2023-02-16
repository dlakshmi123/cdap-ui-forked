/*
 * Workflow History Controller
 */

define([], function () {

  var Controller = Ember.Controller.extend({

    runs: Ember.ArrayProxy.create({
      content: []
    }),

    elements: Em.Object.create(),

    load: function () {
      var model = this.get('model');
      var self = this;
      this.set('elements.Actions', Em.ArrayProxy.create({content: []}));
      for (var i = 0; i < model.actions.length; i++) {
        model.actions[i].state = 'IDLE';
        model.actions[i].isRunning = false;
        model.actions[i].appId = self.get('model').app;
        model.actions[i].divId = model.actions[i].name.replace(' ', '');

        if ('mapReduceName' in model.actions[i].options) {
          var transformedModel = C.Batch.transformModel(model.actions[i]);

          this.get('elements.Actions.content').push(C.Batch.create(transformedModel));
        } else {
          this.get('elements.Actions.content').push(Em.Object.create(model.actions[i]));
        }

      }

      this.HTTP.rest('apps', model.app, 'workflows', model.name, 'history', function (response) {
          if (response) {
            var history = response;

            for (var i = 0; i < history.length; i ++) {

              self.runs.pushObject(C.Run.create(history[i]));

            }
          }

      });

    },

    unload: function () {

      this.set('elements.Actions.content', []);

      this.get('runs').set('content', []);

    }
  });

  Controller.reopenClass({
    type: 'WorkflowHistory',
    kind: 'Controller'
  });

  return Controller;

});