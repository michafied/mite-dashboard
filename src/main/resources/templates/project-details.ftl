<html lang="en">
<head>
    <title>Hello Mite - team dashboard</title>
    <meta content="text/html; charset=utf-8" http-equiv="Content-Type"/>
    <meta charset="utf-8"/>
    <script src="./js/vue.js" type="text/javascript"></script>
    <script src="./js/http.js" type="text/javascript"></script>
    <script src="./js/project-details.js" type="text/javascript"></script>
    <link href="./css/main.css" rel="stylesheet"/>
</head>
<body onload="setup()">
<script type="text/javascript">
    var pId = ${id};
</script>
<div class="box" id="projectDetails">
    <table>
        <thead>
            <th align="left">Project</th>
            <th>available</th>
            <th>used</th>
            <th>ratio</th>
            <th></th>
            <th>archive</th>
        </thead>
        <tbody>
            <tr class="long">
                <td align="left">{{ project.name }}</td>
                <td align="center">{{ project.budget }}</td>
                <td align="center">{{ timeSum }}</td>
                <td align="center">
                    {{ Math.ceil((timeSum / project.budget)*100) }}%
                </td>
                <td>
                    <div class="myProgress">
                        <div class="myBar"
                             v-bind:style="{width: Math.ceil(Math.min(1, timeSum / project.budget)*100)+'%'}"
                             v-bind:class="[((timeSum / project.budget) < 0.95) ? 'inTime' : 'overTime']"></div>
                    </div>
                </td>
                <td align="center" style="min-width: 7em;">
                    <label for="budget">Budget<br/>(in hours)</label>
                    <input @keyup.enter="send" size="20em" type="number" v-model.number="budget"/>
                    <button v-on:click="updateBudget">update budget</button>
                </td>
                <td align="center" style="min-width: 7em;">
                  <a v-if="project.archived" class="archive" v-on:click.stop.prevent="unArchive" alt="to be workable">🗃 → ⏱</a>
                  <a v-else class="archive" v-on:click.stop.prevent="archive" alt="to be archived">🏁 → 🗃</a>
                </td>
            </tr>
        </tbody>
        <tbody v-for="time in projectTimes">
            <tr class="long">
                <td align="left"> ⇱ {{ time.serviceName }}</td>
                <td align="center">{{ project.budget }}</td>
                <td align="center">{{ time.hours }}</td>
                <td align="center">
                    {{ Math.ceil((time.hours / project.budget)*100) }}%
                </td>
                <td>
                    <div class="myProgress">
                        <div class="myBar"
                             v-bind:style="{width: Math.ceil(Math.min(1, time.hours / project.budget)*100)+'%'}"
                             v-bind:class="[((time.hours / project.budget) < 0.95) ? 'inTime' : 'overTime']"></div>
                    </div>
                </td>
                <td align="center"></td>
                <td align="center"></td>
            </tr>
        </tbody>
    </table>
    <select size="1em" v-on:change="assign($event)">
        <option selected value="0">Assign to vProject</option>
        <option v-bind:value="vProject.id" v-for="vProject in vProjects">
            {{vProject.name}}
        </option>
    </select>
</div>

</body>
</html>
