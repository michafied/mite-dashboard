
var store = { signal : 0 };

function setup() {

var createProject = new Vue({
  el: '#createProject',
  data: {
    customers: [],
    name: "",
    budget: 1,
    customer: "",
    feedback: "",
    error: false,
    signal: store
  },
  created: function () {
    var self = this;
    http()
      .get(
        "../customers",
        response => {
          self.customers = JSON.parse(response);
        }
      );
    },
    methods: {
      send: function (event) {
        var self = this;
        window.setTimeout(function(){
          self.feedback = "";
          self.error = false;
        }, 3000);

        http(() => { self.feedback = "loading"; })
          .post(
            "/projects",
            {
              "name": self.name,
              "customerId": parseInt(self.customer, 10),
              "budget": parseInt(self.budget, 10)
            },
            response => {
              self.feedback = "done";
              self.signal.signal++;
            },
            response => {
              self.error = true;
              self.feedback = JSON.parse(response).error;
            },
            xhr => {
              self.error = true;
              self.feedback = "error: "+xhr.status;
            }
          );
      }
    }
});

var projectList = new Vue({
  el: '#projectList',
  data: {
    projects: [],
    projectTimes: {},
    signal: store
  },
  methods: {
    loadTime: function (id) {
      if(id !== undefined) {
        var self = this;
        http()
          .get(
            "../times/"+id,
            response => {
              Vue.set(self.projectTimes, id, JSON.parse(response).hours)
            }
          );
      }
    },
    refresh: function () {
        var self = this;
        http()
          .get(
            "../projects",
            response => {
              var json = JSON.parse(response);
              self.projects = json;
              for(i=0; i< json.length; ++i){
                self.loadTime(json[i].id);
              }
            }
          );
    }
  },
  created: function () {
    this.refresh();
  },
  watch: {
    signal: {
      deep: true,
      handler (val, oldVal) {
        console.log("watched:"+oldVal+" -> "+val);
        this.refresh();
      }
    }
  }
});

}
