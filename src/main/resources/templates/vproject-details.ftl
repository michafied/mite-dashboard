<html lang="en">
<head>
    <title>Hello Mite - team dashboard</title>
    <meta content="text/html; charset=utf-8" http-equiv="Content-Type"/>
    <meta charset="utf-8"/>
    <script src="./js/vue.js" type="text/javascript"></script>
    <script src="./js/http.js" type="text/javascript"></script>
    <script src="./js/vproject-details.js" type="text/javascript"></script>
    <link href="./css/main.css" rel="stylesheet"/>
</head>
<body onload="setup()">
<script type="text/javascript">
    var pId = ${id};
</script>
<div class="box" id="vProjectDetails">
    <table>
        <thead>
            <th align="left">{{ vProject.name }}</th>
            <th align="center">{{ vBudget() }}</th>
            <th align="center">{{ vUsed() }}</th>
            <th align="center">{{ Math.ceil((vUsed() / vBudget())*100) }}%</th>
            <th>
                <div class="myProgress">
                    <div class="myBar"
                         v-bind:style="{width: Math.ceil(Math.min(1, vUsed() / vBudget())*100)+'%'}"
                         v-bind:class="[((vUsed() / vBudget()) < 0.95) ? 'inTime' : 'overTime']"></div>
                </div>
            </th>
        </thead>
        <tbody v-for="project in vProject.children">
            <tr class="long">
                <td align="left"> ⇱ {{ project.name }}</td>
                <td align="center">{{ project.budget }}</td>
                <td align="center">{{ pUsed(project) }}</td>
                <td align="center">{{ Math.ceil((pUsed(project) / project.budget)*100) }}%</td>
                <td>
                    <div class="myProgress">
                        <div class="myBar"
                             v-bind:style="{width: Math.ceil(Math.min(1, pUsed(project) / project.budget)*100)+'%'}"
                             v-bind:class="[((pUsed(project) / project.budget) < 0.95) ? 'inTime' : 'overTime']"></div>
                    </div>
                </td>
            </tr>
            <tr class="long" v-for="time in project.times">
                <td align="left">&nbsp;&nbsp;&nbsp;&nbsp;- {{ time.serviceName }}</td>
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
            </tr>
        </tbody>
    </table>
</div>

</body>
</html>
