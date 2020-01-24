<html lang="en">
<head>
    <title>Hello Mite - team dashboard</title>
    <meta content="text/html; charset=utf-8" http-equiv="Content-Type"/>
    <meta charset="utf-8"/>
    <script src="./js/vue.js" type="text/javascript"></script>
    <script src="./js/http.js" type="text/javascript"></script>
    <script src="./js/details.js" type="text/javascript"></script>
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
            <tr>
                <td align="left">{{ project.name }}</td>
                <td align="center">{{ project.budget }}</td>
                <td align="center">{{ projectTime }}</td>
                <td align="center">
                    {{ Math.ceil((projectTime / project.budget)*100) }}%
                </td>
                <td>
                    <div class="myProgress">
                        <div class="myBar"
                             v-bind:style="{width: Math.ceil(Math.min(1, projectTime / project.budget)*100)+'%'}"
                             v-bind:class="[((projectTime / project.budget) < 0.95) ? 'inTime' : 'overTime']"></div>
                    </div>
                </td>
                <td align="center">
                  <a v-if="project.archived" class="archive" v-on:click.stop.prevent="unArchive" alt="to be workable">🗃 → ⏱</a>
                  <a v-else class="archive" v-on:click.stop.prevent="archive" alt="to be archived">🏁 → 🗃</a>
                </td>
            </tr>
        </tbody>
    </table>
</div>

</body>
</html>
