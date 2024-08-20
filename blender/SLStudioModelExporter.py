# Needs to be manually reloaded in blender after changing

# To setup on an empty Blender file, open script in text editor and run.
# Enable "Register" in "Text" menu to run on startup. Reload script with
# "Alt-R" and run with "Alt-P" in the Text Editor pane.
#
# Start with an empty Blend file and click "Setup Scene" in SLStudio panel.
# Then click "Setup All Fixtures". Duplicate the collection under "Template"
# and move into "Fixtures" to get started. You can move the top-level empty of
# a fixture to set its location, edit the curve to change the shape, and
# change the count and X constant offset of the mesh Array modifier. You can
# also duplicate a mesh and slide it down the curve to have more control over
# LED spacing.

import os
import re
import datetime
import json
import bpy

# ExportHelper is a helper class, defines filename and
# invoke() function which calls the file selector.
from bpy_extras.io_utils import ExportHelper
from bpy.props import StringProperty

LOG_TAG = '[SLStudioModelExporter]'
OBJECT_NAME_REGEXP = re.compile(r'^(Fixture|Shape)(_\w+)?\.(\d+)$')
FIXTURE_NAME_REGEXP = re.compile(r'^Fixture\.(\d+)$')
SHAPE_NAME_REGEXP = re.compile(r'^Shape\.(\d+)$')
OUTPUT_NAME_REGEXP = re.compile(r'^Controller\.(\d+)_Output\.(\d+)$')

SCENERY_COLLECTION = 'Scenery'
TEMPLATE_COLLECTION = 'Template'
FIXTURES_COLLECTION = 'Fixtures'

#PROP_OBJECT_TYPE = 'SL_ObjectType';
PROP_FIXTURE_TYPE = 'SL_FixtureType';
PROP_FIXTURE_COLOR_TYPE = 'SL_FixtureColorType';
PROP_FIXTURE_CONTROLLER = 'SL_FixtureController';
PROP_FIXTURE_OUTPUT = 'SL_FixtureOutput';
PROP_FIXTURE_REVERSE = 'SL_FixtureReverse';
PROP_FIXTURE_DETECT_FOLDS = 'SL_FixtureDetectFolds';
PROP_OUTPUT_TYPE = 'SL_OutputType';
#PROP_CONTROLLER_ID = 'SL_ControllerId';
#PROP_CONTROLLER_TYPE = 'SL_ControllerType';
#PROP_CONTROLLER_IP_ADDRESS = 'SL_ControllerIpAddress';
#PROP_CONTROLLER_UNIVERSES_PER_OUTPUT = 'SL_ControllerUniversesPerOutput';

OBJECT_TYPE_FIXTURE = 'FIXTURE';
OBJECT_TYPE_CONTROLLER = 'CONTROLLER';

def extract_vertex_coords(depsgraph, ob_main):
    print(LOG_TAG, 'Extracting vertex coords from ' + ob_main.name)
    vcos = []

    obs = [(ob_main, ob_main.matrix_world)]
    if ob_main.is_instancer:
        obs += [(dup.instance_object.original, dup.matrix_world.copy())
                    for dup in depsgraph.object_instances
                    if dup.parent and dup.parent.original == ob_main]

    print(LOG_TAG, 'obs size: %d' % len(obs));

    for ob, ob_matrix in obs:
        if ob.type == 'EMPTY':
            for child in ob.children:
                #if child.name.startswith(ob.name):
                if child.type == 'MESH':
                    print(LOG_TAG, 'empty has mesh child');
                    vcos.extend(extract_vertex_coords(depsgraph, child))
        else:
            # apply modifiers
            ob_for_convert = ob.evaluated_get(depsgraph) # else ob.original

            try:
                try:
                    mesh = ob_for_convert.to_mesh()
                    if mesh is not None:
                        print(LOG_TAG, 'has %d verts' % len(mesh.vertices));
                        mesh.transform(ob_matrix)
                        for v in mesh.vertices:
                            vcos.append(v.co[:])
                    else:
                        raise Exception('ob_for_convert.to_mesh() is null')
                except Exception as e:
                    print(LOG_TAG, 'Could not convert object "%s" to mesh: %s' % (ob.name, e))
                    continue
            finally:
                # clean up
                ob_for_convert.to_mesh_clear()
    if PROP_FIXTURE_REVERSE in ob_main and ob_main[PROP_FIXTURE_REVERSE]:
        print(LOG_TAG, 'Reversing object "%s"' % ob_main.name)
        vcos = reversed(vcos)
    return vcos

def filter_fixture_objects(objects):
    objects = objects[:]
    objects += [x.parent for x in objects if x.parent]
    objects = [x for x in objects
                    if x.type == 'EMPTY'
                    and re.match(FIXTURE_NAME_REGEXP, x.name or '')]
    return list(set(objects))

class SLStudioModelExporter(bpy.types.Operator, ExportHelper):
    """SLStudio Model Export""" # tooltip
    bl_idname = "export_scene.sl_model"  # for bpy.ops.export_scene.sl_model
    bl_label = "Export SLStudio Model"

    # ExportHelper mixin class uses this
    filename_ext = ".json"

    filter_glob: StringProperty(
        default="*.json",
        options={'HIDDEN'},
        maxlen=255,  # Max internal buffer length, longer would be clamped.
    )

    def invoke(self, context, _event):
        dirname = os.path.dirname(context.blend_data.filepath)
        self.filepath = os.path.join(dirname, 'model.json')

        return super().invoke(context, _event)

    def model_gen(self, context):
        # based on Blender's export_obj addon

        depsgraph = context.evaluated_depsgraph_get()
        scene = context.scene
        objects = scene.objects

        out = {
            'date': datetime.datetime.now().isoformat(),
            'notes': "Exported from Blender using SLStudioModelExporter script",
            'fixtures': [],
            'controllers': [],
        }

        for ob in objects:
            ob_name = ob.name or ''

            m = re.match(FIXTURE_NAME_REGEXP, ob_name)
            if not m or ob.type != 'EMPTY':
                print(LOG_TAG, 'Skipping object "%s"' % ob.name)
                continue

            # ignore fixtures inside the Template collection
            if len([c for pc in bpy.data.collections[TEMPLATE_COLLECTION].children_recursive for c in ob.users_collection if pc == c]) > 0:
                continue

            print(LOG_TAG, 'Writing object "%s"' % ob_name)

            fixture_idx = int(m[1]) - 1
            controller_idx = None
            output_idx = None

            if fixture_idx < 0:
                print(LOG_TAG, 'Skipping object "%s" with negative idx' % ob.name)
                continue

            for c in ob.users_collection:
                mc = re.match(OUTPUT_NAME_REGEXP, c.name or '')
                if mc:
                    [controller_idx, output_idx] = mc.groups()
                    if controller_idx is not None:
                        controller_idx = int(controller_idx) - 1
                    if output_idx is not None:
                        output_idx = int(output_idx) - 1
                    break

            if output_idx is None and PROP_FIXTURE_OUTPUT in ob and ob[PROP_FIXTURE_OUTPUT] > 0:
                # use FixtureOutput object property for output number
                if PROP_FIXTURE_CONTROLLER in ob and ob[PROP_FIXTURE_CONTROLLER] > 0:
                    controller_idx = ob[PROP_FIXTURE_CONTROLLER] - 1
                output_idx = ob[PROP_FIXTURE_OUTPUT] - 1

            fixture_type = 'GENERIC'
            if PROP_FIXTURE_TYPE in ob and ob[PROP_FIXTURE_TYPE]:
                fixture_type = ob[PROP_FIXTURE_TYPE].upper()

            fixture_color_type = 'RGB'
            if PROP_FIXTURE_COLOR_TYPE in ob and ob[PROP_FIXTURE_COLOR_TYPE]:
                fixture_color_type = ob[PROP_FIXTURE_COLOR_TYPE].upper()

            detect_folds = False
            if PROP_FIXTURE_DETECT_FOLDS in ob and ob[PROP_FIXTURE_DETECT_FOLDS]:
                detect_folds = bool(ob[PROP_FIXTURE_DETECT_FOLDS])

            fixture_verts = extract_vertex_coords(depsgraph, ob)
            shape_verts = None

            for child_ob in ob.children:
                ms = re.match(SHAPE_NAME_REGEXP, child_ob.name or '')
                if ms:
                    shape_verts = extract_vertex_coords(depsgraph, child_ob)
                    break

            fixture = {
                'name': ob_name,
                'idx': fixture_idx,
                'type': fixture_type,
                'colorType': fixture_color_type,
                'detectFolds': detect_folds,
                'verts': fixture_verts,
                'shapeVerts': shape_verts,
                'controllerIdx': controller_idx,
                'outputIdx': output_idx,
            }
            out['fixtures'].append(fixture)

        controllers_by_idx = {}

        for collection in bpy.context.scene.collection.children_recursive:
            # ignore outputs inside the Template collection
            if len([c for c in bpy.data.collections[TEMPLATE_COLLECTION].children_recursive if c == collection]) > 0:
                continue

            mc = re.match(OUTPUT_NAME_REGEXP, collection.name or '')
            if mc:
                [controller_idx, output_idx] = mc.groups()
                if controller_idx is None or output_idx is None:
                    continue

                controller_idx = int(controller_idx) - 1
                output_idx = int(output_idx) - 1

                if controller_idx < 0 or output_idx < 0:
                    print(LOG_TAG, 'Skipping output "%s" with negative idx' % collection.name)
                    continue

                output_type = 'GENERIC'
                if PROP_OUTPUT_TYPE in ob and ob[PROP_OUTPUT_TYPE]:
                    output_type = ob[PROP_OUTPUT_TYPE].upper()

                output = {
                    'idx': output_idx,
                    'name': collection.name,
                    'type': output_type,
                }

                controller = controllers_by_idx.setdefault(controller_idx, {
                    'idx': controller_idx,
                    'outputs': [],
                })
                controller['outputs'].append(output)

        controllers = list(controllers_by_idx.values())
        controllers.sort(key=lambda x: x['idx'])
        for controller in controllers:
            controller['outputs'].sort(key=lambda x: x['idx'])

        out['controllers'] = controllers

        return out

    def execute(self, context):
        print(LOG_TAG, 'Exporting SLStudio model to file "%s"' % self.filepath)
        model_json = json.dumps(self.model_gen(context), indent=2)
        f = open(self.filepath, 'w', encoding='utf-8')
        f.write(model_json)
        f.close()
        print(LOG_TAG, 'Finished')

        return {'FINISHED'}

class SLStudioFixtureProperties(bpy.types.PropertyGroup):
    FixtureType: bpy.props.StringProperty(name="Fixture Type", description="SLStudio: Fixture Type")
    ReverseOrder: bpy.props.BoolProperty(name="Reverse Order", description="SLStudio: Reverse Point Order")

class SelectObjectByNameOperator(bpy.types.Operator):
    """Select Object By Name"""
    bl_idname = "object.select_object_by_name"
    bl_label = "Select Object By Name"
    bl_options = {'REGISTER', 'UNDO'}

    object_name: bpy.props.StringProperty(name="Object name")
    action: bpy.props.EnumProperty(items=(('TOGGLE', "Toggle", ""), ('SELECT', "Select", ""), ('DESELECT', "Deselect", "")), default='SELECT')
    apply_to_children: bpy.props.BoolProperty(name="Apply to children", default=False)

    def execute(self, context):
        if self.object_name and self.object_name in bpy.data.objects \
                and bpy.data.objects[self.object_name] in context.selectable_objects:
            obj = bpy.data.objects[self.object_name]
            if self.action == 'SELECT':
                state = True
            elif self.action == 'DESELECT':
                state = False
            else:
                state = not obj.select_get()
            obj.select_set(state=state)
            context.view_layer.objects.active = obj if state else None

            if self.apply_to_children:
                for child in obj.children:
                    child.select_set(state=state)
        return {'FINISHED'}

class SLStudioSelectAllFixturesOperator(bpy.types.Operator):
    """SLStudio Select All Fixtures Operator"""
    bl_idname = "object.slstudio_select_all_fixtures"
    bl_label = "Select All Fixtures"
    bl_options = {'REGISTER', 'UNDO'}

    action: bpy.props.EnumProperty(items=(('TOGGLE', "Toggle", ""), ('SELECT', "Select", ""), ('DESELECT', "Deselect", "")), default='SELECT')
    apply_to_children: bpy.props.BoolProperty(name="Apply to children", default=False)

    def execute(self, context):
        objects = filter_fixture_objects(context.selectable_objects)
        for obj in objects:
            if self.action == 'SELECT':
                state = True
            elif self.action == 'DESELECT':
                state = False
            else:
                state = not obj.select_get()
            obj.select_set(state=state)

            if self.apply_to_children:
                for child in obj.children:
                    child.select_set(state=state)
        return {'FINISHED'}

class SLStudioSetupSceneOperator(bpy.types.Operator):
    """SLStudio Setup Scene Operator"""
    bl_idname = "object.slstudio_setup_collections"
    bl_label = "Setup Scene collections and units"
    bl_options = {'REGISTER', 'UNDO'}

    def execute(self, context):
        # setup units
        bpy.context.scene.unit_settings.system = 'IMPERIAL'
        bpy.context.scene.unit_settings.length_unit = 'INCHES'
        bpy.context.scene.unit_settings.scale_length = 0.0254  # 1 inch = 0.0254 meters
        bpy.context.scene.unit_settings.use_separate = True

        # setup default collections
        for collection_name in [SCENERY_COLLECTION, TEMPLATE_COLLECTION, FIXTURES_COLLECTION]:
            if collection_name not in bpy.context.scene.collection.children:
                collection = bpy.data.collections.new(collection_name)
                bpy.context.scene.collection.children.link(collection)

                if collection_name == TEMPLATE_COLLECTION:
                    collection.hide_select = True
                    collection.hide_render = True
                    #collection.hide_viewport = True
                    #bpy.context.view_layer.layer_collection.children[TEMPLATE_COLLECTION].exclude = True

                    controller_collection = bpy.data.collections.new('Controller.001_Output.000')
                    collection.children.link(controller_collection)

                    # create example fixture with shape and LED strip
                    fixture_name = 'Fixture.000'
                    shape_name = 'Shape.000'
                    strip_name = 'Strip.000'

                    # create parent Fixture as empty
                    fixture = bpy.data.objects.new(fixture_name, None)
                    controller_collection.objects.link(fixture)

                    # create Shape as curve
                    shape_data = bpy.data.curves.new(shape_name, 'CURVE')
                    shape_data.dimensions = '3D'
                    shape_data.resolution_u = 12
                    nurbs = shape_data.splines.new('NURBS')
                    nurbs.points.add(3)
                    nurbs.points[0].co = (0, 0, 0, 1)
                    nurbs.points[1].co = (4, 0, 0, 1)
                    nurbs.points[2].co = (8, 0, 0, 1)
                    nurbs.points[3].co = (12, 0, 0, 1)
                    nurbs.order_u = 4
                    nurbs.use_endpoint_u = True
                    shape = bpy.data.objects.new(shape_name, shape_data)
                    shape.parent = fixture
                    shape.lock_location = [True, True, True]
                    shape.lock_rotation = [True, True, True]
                    controller_collection.objects.link(shape)

                    # create Strip as mesh with Array and Curve modifiers
                    strip_data = bpy.data.meshes.new(strip_name)
                    strip_data.from_pydata([(0, 0, 0)], [], []) # single point at origin
                    strip = bpy.data.objects.new(strip_name, strip_data)
                    strip.parent = fixture
                    strip.lock_location = [False, True, True] # can move along X-axis
                    strip.lock_rotation = [True, True, True]
                    strip.lock_scale = [True, True, True]
                    controller_collection.objects.link(strip)

                    array_modifier = strip.modifiers.new('Array', 'ARRAY')
                    array_modifier.count = 10
                    array_modifier.use_relative_offset = False
                    array_modifier.use_constant_offset = True
                    array_modifier.constant_offset_displace = (1, 0, 0) # 1 inch pitch
                    curve_modifier = strip.modifiers.new('Curve', 'CURVE')
                    curve_modifier.object = shape
                    curve_modifier.deform_axis = 'POS_X'

                    # show selectable toggle and hide render toggle by default
                    for area in bpy.data.screens['Layout'].areas:
                        if area.type == 'OUTLINER':
                            area.spaces[0].show_restrict_column_select = True
                            area.spaces[0].show_restrict_column_render = False

        return {'FINISHED'}

class SLStudioSetupFixtureOperator(bpy.types.Operator):
    """SLStudio Setup Fixture Operator"""
    bl_idname = "object.slstudio_setup_fixtures"
    bl_label = "Setup All or Selected Fixtures"
    bl_options = {'REGISTER', 'UNDO'}

    all_or_selected: bpy.props.EnumProperty(name='All or Selected', items=(
                        ('ALL', "All", ""), ('SELECTED', "Selected", "")))

    def execute(self, context):
        objects = filter_fixture_objects(bpy.data.objects if self.all_or_selected == 'ALL' else context.selected_objects)

        for object in objects:
            object[PROP_FIXTURE_TYPE] = object.get(PROP_FIXTURE_TYPE) or ''
            object[PROP_FIXTURE_COLOR_TYPE] = object.get(PROP_FIXTURE_COLOR_TYPE) or ''
            object[PROP_FIXTURE_REVERSE] = 1 if object.get(PROP_FIXTURE_REVERSE) else 0
            object[PROP_FIXTURE_DETECT_FOLDS] = 1 if object.get(PROP_FIXTURE_DETECT_FOLDS) else 0
            #object.id_properties_ui(PROP_FIXTURE_TYPE).update(description='SLStudio: Fixture type')
            #object.id_properties_ui(PROP_FIXTURE_REVERSE).update(description='SLStudio: Reverse point order', min=0, max=1, soft_min=0, soft_max=1)
        return {'FINISHED'}

class SLStudioSetFixturePropertiesOperator(bpy.types.Operator):
    """SLStudio Set Fixture Properties Operator"""
    bl_idname = "object.slstudio_set_fixture_properties"
    bl_label = "Set Properties on Selected Fixtures"
    bl_options = {'REGISTER', 'UNDO'}

    FixtureType: bpy.props.StringProperty(name='Fixture Type', description="SLStudio: Fixture Type", default="Generic")
    FixtureColorType: bpy.props.StringProperty(name='Fixture Color Type', description="SLStudio: Fixture Color Type", default="RGB")
    ReverseOrder: bpy.props.BoolProperty(name='Reverse Order', description="SLStudio: Reverse Point Order")
    DetectFolds: bpy.props.BoolProperty(name='Detect Folds', description="SLStudio: Detect Folds")

    def invoke(self, context, event):
        wm = context.window_manager
        return wm.invoke_props_dialog(self)

    def execute(self, context):
        objects = filter_fixture_objects(context.selected_objects)

        bpy.ops.object.slstudio_setup_fixtures(all_or_selected='SELECTED')

        for object in objects:
            object[PROP_FIXTURE_TYPE] = self.FixtureType
            object[PROP_FIXTURE_COLOR_TYPE] = self.FixtureColorType
            object[PROP_FIXTURE_REVERSE] = self.ReverseOrder
            object[PROP_FIXTURE_DETECT_FOLDS] = self.DetectFolds

        return {'FINISHED'}

class SLStudioFixturePropertiesPanel(bpy.types.Panel):
    """SLStudio Fixture Properties Panel"""
    bl_idname = "SCENE_PT_slstudio_fixture_properties"
    bl_label = "Fixture Properties"
    bl_space_type = 'VIEW_3D'
    bl_region_type = 'UI'
    bl_category = "SLStudio"
    bl_options = {'HEADER_LAYOUT_EXPAND'}

    def draw(self, context):
        layout = self.layout
        objects = filter_fixture_objects(bpy.data.objects)

        layout.operator(SLStudioSetupSceneOperator.bl_idname, text='Setup Scene')
        layout.operator(SLStudioSetupFixtureOperator.bl_idname, text='Setup All Fixtures').all_or_selected = 'ALL'
        layout.operator(SLStudioModelExporter.bl_idname, text='Export')

        row = layout.row()
        row.label(text="{} Fixtures".format(len(objects)), icon='WORLD_DATA')
        props = row.operator(SLStudioSelectAllFixturesOperator.bl_idname, text='Select All')
        props.action='SELECT'
        props.apply_to_children = False
        props = row.operator(SLStudioSelectAllFixturesOperator.bl_idname, text='None')
        props.action='DESELECT'
        props.apply_to_children = True
        layout.operator(SLStudioSetFixturePropertiesOperator.bl_idname)

        if not len(objects):
            return

        for object in objects:
            row = layout.row(heading=object.name)
            row.prop(object, '["'+PROP_FIXTURE_TYPE+'"]', text='Type')
            row.prop(object, '["'+PROP_FIXTURE_COLOR_TYPE+'"]', text='ColorType')
            row.prop(object, '["'+PROP_FIXTURE_REVERSE+'"]', text='Reverse?')
            row.prop(object, '["'+PROP_FIXTURE_DETECT_FOLDS+'"]', text='Detect Folds?')
            col = row.column()
            props = col.operator(SelectObjectByNameOperator.bl_idname, text=('Deselect' if object.select_get() else 'Select'))
            props.object_name = object.name
            props.action = 'TOGGLE'
            props.apply_to_children = True
            col.enabled = object in context.selectable_objects
            #layout.prop(object, 'slstudio_props')

# Only needed if you want to add into a dynamic menu
def menu_func_export(self, context):
    self.layout.operator(SLStudioModelExporter.bl_idname, text="SLStudio Model")


classes = [
    SLStudioFixtureProperties,
    SLStudioModelExporter,
    SelectObjectByNameOperator,
    SLStudioSelectAllFixturesOperator,
    SLStudioSetupSceneOperator,
    SLStudioSetupFixtureOperator,
    SLStudioSetFixturePropertiesOperator,
    SLStudioFixturePropertiesPanel
]

# Register and add to the "file selector" menu (required to use F3 search "Text Export Operator" for quick access).
def register():
    for cls in classes:
        bpy.utils.register_class(cls)

    bpy.types.TOPBAR_MT_file_export.append(menu_func_export)
    #bpy.types.Scene.slstudio_props = bpy.props.PointerProperty(type=SLStudioFixtureProperties)

def unregister():
    #del bpy.types.Scene.slstudio_props
    bpy.types.TOPBAR_MT_file_export.remove(menu_func_export)

    for cls in reversed(classes):
        bpy.utils.unregister_class(cls)
    bpy.utils.unregister_class(SLStudioFixturePropertiesPanel)
    bpy.utils.unregister_class(SLStudioSetupSceneOperator)
    bpy.utils.unregister_class(SLStudioSetupFixtureOperator)
    bpy.utils.unregister_class(SLStudioModelExporter)
    bpy.utils.unregister_class(SLStudioFixtureProperties)


if __name__ == "__main__":
    register()
