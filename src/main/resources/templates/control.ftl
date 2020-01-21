<html lang="en">
<head>
    <title>Hello Mite</title>
    <meta content="text/html; charset=utf-8" http-equiv="Content-Type"/>
    <meta charset="utf-8"/>
    <script src="js/vue.js" type="text/javascript"></script>
    <script src="js/http.js" type="text/javascript"></script>
    <script src="js/mite.js" type="text/javascript"></script>
    <link href="css/main.css" rel="stylesheet"/>
</head>
<body onload="setup()">

<div class="box" id="createProject">
    <label for="name">Name<br/>(needs to match ${projectSpec.nameMatches})</label>
    <input @keyup.enter="send" size="20em" type="text" v-model.trim="name"/><br/>
    <label for="budget">Budget<br/>(in hours)</label>
    <input @keyup.enter="send" size="20em" type="number" v-model.number="budget"/><br/>
    <label for="customer">Customer</label>
    <select name="customer" size="1em" v-model="customer">
        <option disabled value="">Please select one</option>
        <option v-bind:value="customer.id" v-for="customer in customers">
            {{customer.name}}
        </option>
    </select><br/>
    <button v-on:click="send">create</button>
    <p v-bind:class="[ error ? 'error' : 'success' ]">{{ feedback }}</p>
</div>
<br/>
<div style="width: 80em">
    <hr style="width:50%">
</div>
<br/>
<div class="box" id="projectList">
    <table>
        <thead>
        <th align="left">Project</th>
        <th>available</th>
        <th>used</th>
        <th>ratio</th>
        <th></th>
        </thead>

        <tr v-for="project in projects">
            <td align="left">{{ project.name }}</td>
            <td align="center">{{ project.budget }}</td>
            <td align="center">{{ projectTimes[project.id] }}</td>
            <td align="center">
                {{ Math.ceil((projectTimes[project.id] / project.budget)*100) }}%
            </td>
            <td>
                <div class="myProgress">
                    <div class="myBar inTime"
                         v-bind:style="{width: Math.ceil(Math.min(1, projectTimes[project.id] / project.budget)*100)+'%'}"
                         v-if="(projectTimes[project.id] / project.budget) < 0.95"></div>
                    <div class="myBar overTime"
                         v-bind:style="{width: Math.ceil(Math.min(1, projectTimes[project.id] / project.budget)*100)+'%'}"
                         v-else></div>
                </div>
            </td>
        </tr>
    </table>
</div>

</body>
</html>
