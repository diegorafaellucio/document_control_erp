// a selection of free myDiagram from https://icomoon.io/
// the following paths are all pure SVG path strings that GoJS parses as Geometry strings.

var icons = {
    "Regras":
        "M29.181 19.070c-1.679-2.908-0.669-6.634 2.255-8.328l-3.145-5.447c-0.898 0.527-1.943 0.829-3.058 0.829-3.361 0-6.085-2.742-6.085-6.125h-6.289c0.008 1.044-0.252 2.103-0.811 3.070-1.679 2.908-5.411 3.897-8.339 2.211l-3.144 5.447c0.905 0.515 1.689 1.268 2.246 2.234 1.676 2.903 0.672 6.623-2.241 8.319l3.145 5.447c0.895-0.522 1.935-0.82 3.044-0.82 3.35 0 6.067 2.725 6.084 6.092h6.289c-0.003-1.034 0.259-2.080 0.811-3.038 1.676-2.903 5.399-3.894 8.325-2.219l3.145-5.447c-0.899-0.515-1.678-1.266-2.232-2.226zM16 22.479c-3.578 0-6.479-2.901-6.479-6.479s2.901-6.479 6.479-6.479c3.578 0 6.479 2.901 6.479 6.479s-2.901 6.479-6.479 6.479z",
};

function init() {
    var $ = go.GraphObject.make;

    myDiagram =
        $(go.Diagram, "diagrama",
            {
                allowCopy: false,
                allowDelete: false,
                allowMove: false,
                initialAutoScale: go.Diagram.UniformToFill,
                layout: $(go.LayeredDigraphLayout),
                "toolManager.hoverDelay": 100
            });

    var lay = myDiagram.layout;
    lay.direction = parseFloat(90, 10);
    lay.setsPortSpots = false;

    function nodeStyle() {
        return [
            new go.Binding("location", "loc", go.Point.parse).makeTwoWay(go.Point.stringify),
            {
                locationSpot: go.Spot.Center
            }
        ];
    }

    function textStyle() {
        return {
            font: "bold 11pt Helvetica, Arial, sans-serif",
            stroke: "#010101"
        }
    }

    function textStyleDesc() {
        return {
            font: "italic 10pt Helvetica, Arial, sans-serif",
        }
    }

     function showToolTip(obj, diagram, tool) {
        var toolTipDIV = document.getElementById('toolTipDIV');
        var pt = diagram.lastInput.viewPoint;
        toolTipDIV.style.left = (pt.x + 10) + "px";
        toolTipDIV.style.top = (pt.y + 10) + "px";
        var regrasIds = obj.Ra;
        var tamanho = regrasIds.length;
        document.getElementById('toolTipParagraph').innerHTML = "";
        for(var item in regrasIds) {
           document.getElementById('toolTipParagraph').innerHTML = document.getElementById('toolTipParagraph').innerHTML + '<a href=\"'+ contextPath + '/cadastros/regras/subregras/'+ regrasIds[item] +'\" target=\"_blank\">' + regrasFluxo[regrasIds[item]] + '</a>';
           if((tamanho-1) != item){
               document.getElementById('toolTipParagraph').innerHTML = document.getElementById('toolTipParagraph').innerHTML + '<br/>';
           }
        }
        toolTipDIV.style.display = "block";
      }

      function hideToolTip(diagram, tool) {
       var toolTipDIV = document.getElementById('toolTipDIV');
       toolTipDIV.style.display = "none";
      }

      var myToolTip = $(go.HTMLInfo, {
        show: showToolTip,
        hide: hideToolTip
      });

    function geoFunc(geoname) {
        var geo = icons["Regras"];  // use this for an unknown icon name
        geo = icons[geoname] = go.Geometry.parse(geo, true);  // fill each geometry
        return geo;
    }

    myDiagram.nodeTemplateMap.add("",
        $(go.Node, "Spot",
            $(go.Panel, "Auto",
                $(go.Shape, "Rectangle",
                    { fill: "#ffffff", strokeWidth: 2.5 },
                    new go.Binding("stroke", "stroke")),
                     $(go.TextBlock,textStyle(),
                        {
                            margin: 8,
                            maxSize: new go.Size(160, NaN),
                            wrap: go.TextBlock.WrapFit,
                            editable: false
                        },
                        new go.Binding("text").makeTwoWay(),
                        { click: function(e, obj) { abrirModalKey({situacaoId: obj.part.data.key}); }}),
            ),
            $(go.Panel, "Vertical",
                { alignment: go.Spot.Left, alignmentFocus: go.Spot.Bottom },
                $("Button",  new go.Binding("visible", "visibleRD"), new go.Binding("name", "descRD"),
                    $(go.Shape,
                        { margin: 1, fill: "blue", strokeWidth: 0, width: 8, height: 8 },
                        new go.Binding("geometry", geoFunc)),
                    {
                        toolTip: myToolTip
                }),
                $("Button",  new go.Binding("visible", "visibleRI"), new go.Binding("name", "descRI"),
                    $(go.Shape,
                        { margin: 1, fill: "red", strokeWidth: 0, width: 8, height: 8 },
                        new go.Binding("geometry", geoFunc)),
                    {
                        toolTip: myToolTip
                    }),
                $("Button",  new go.Binding("visible", "visibleRA"), new go.Binding("name", "descRA"),
                    $(go.Shape,
                        { margin: 1, fill: "yellow", strokeWidth: 0, width: 8, height: 8 },
                        new go.Binding("geometry", geoFunc)),
                    {
                        toolTip: myToolTip
                    })
            )));

    myDiagram.linkTemplate =
        $(go.Link,
            {
                curve: go.Link.Bezier,
                corner: 1,
                toShortLength: 2,
            },
            new go.Binding("points").makeTwoWay(),
            $(go.Shape,  // a forma de destaque, normalmente transparente
                { isPanelMain: true, strokeWidth: 5, stroke: "transparent", name: "HIGHLIGHT" }),
            $(go.Shape,  // a forma do caminho do link
                { isPanelMain: true, stroke: "green", strokeWidth: 2 },
                new go.Binding("stroke", "isSelected", function(sel) { return sel ? "blue" : "green"; }).ofObject()),
            $(go.Shape,  // a ponta da flecha
                { toArrow: "standard", strokeWidth: 0, fill: "green" }),
            $(go.Panel, "Auto",  // o rótulo do link, normalmente não visível
                { visible: false, name: "LABEL", segmentIndex: 2, segmentFraction: 0.5 },
                new go.Binding("visible", "visible").makeTwoWay(),
                $(go.Shape, "RoundedRectangle",  // a forma da etiqueta
                    { fill: "#F8F8F8", strokeWidth: 0 }),
                $(go.TextBlock, "Yes",  // o rótulo
                    {
                        textAlign: "center",
                        font: "10pt helvetica, arial, sans-serif",
                        stroke: "#333333",
                        editable: false
                    },
                    new go.Binding("text").makeTwoWay())
            )
        );
    load();
}