# Needs to be manually reloaded in blender after changing

import os
import re
import datetime
import bpy

# ExportHelper is a helper class, defines filename and
# invoke() function which calls the file selector.
from bpy_extras.io_utils import ExportHelper
from bpy.props import StringProperty

LOG_TAG = '[SLStudioObjExporter]'
OBJECT_NAME_REGEXP = re.compile(r'^(Fixture|Shape)(_\w+)?\.(\d+)$')
FIXTURE_NAME_REGEXP = re.compile(r'^Fixture\.(\d+)$')
OUTPUT_NAME_REGEXP = re.compile(r'^(?:Controller\.(\d+)_)?Output\.(\d+)$')

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
                except RuntimeError as e:
                    print(LOG_TAG, 'Could not convert object "%s" to mesh: %s' % (ob.name, e))
                    continue

                print(LOG_TAG, 'has %d verts' % len(ob.to_mesh().vertices));
                mesh.transform(ob_matrix)

                for v in mesh.vertices:
                    vcos.append(v.co[:])
            finally:
                # clean up
                ob_for_convert.to_mesh_clear()
    if 'ReverseOrder' in ob_main and ob_main['ReverseOrder']:
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

class SLStudioObjExporter(bpy.types.Operator, ExportHelper):
    """SLStudio Obj Export""" # tooltip
    bl_idname = "export_scene.sl_obj"  # for bpy.ops.export_scene.sl_obj
    bl_label = "Export Model Obj"

    # ExportHelper mixin class uses this
    filename_ext = ".obj"

    filter_glob: StringProperty(
        default="*.obj",
        options={'HIDDEN'},
        maxlen=255,  # Max internal buffer length, longer would be clamped.
    )

    def invoke(self, context, _event):
        dirname = os.path.dirname(context.blend_data.filepath)
        self.filepath = os.path.join(dirname, 'model.obj')

        return super().invoke(context, _event)

    def obj_gen(self, context):
        # based on Blender's export_obj addon

        depsgraph = context.evaluated_depsgraph_get()
        scene = context.scene
        objects = scene.objects

        out_strs = [
            "# Exported using SLStudioObjExporter script\n",
            "# Date: %s UTC (%s local time)\n" % (datetime.datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S"), datetime.datetime.now().strftime("%H:%M:%S")),
            "\n"
        ]

        for ob in objects:
            ob_name = ob.name or ''

            m = re.match(OBJECT_NAME_REGEXP, ob_name)
            if not m:
                print(LOG_TAG, 'Skipping object "%s"' % ob.name)
                continue

            if len([c for c in ob.users_collection if c.name == 'Template']) > 0:
                continue

            if m[1] == 'Fixture':
                collections = [c for c in ob.users_collection
                                if re.match(OUTPUT_NAME_REGEXP, c.name or '')]
                if len(collections) > 0:
                    # use collection name for output number (e.g. Output.001)
                    ob_name += '_' + collections[0].name
                elif 'FixtureOutput' in ob and ob['FixtureOutput'] > 0:
                    # use FixtureOutput object property for output number
                    if 'FixtureController' in ob and ob['FixtureController'] > 0:
                        ob_name += '_Controller.' + format(ob['FixtureController'], '03')
                    ob_name += '_Output.' + format(ob['FixtureOutput'], '03')

                if 'FixtureType' in ob and ob['FixtureType']:
                    # use FixtureOutput object property for output number
                    ob_name += '_Type.' + ob['FixtureType']

            print(LOG_TAG, 'Writing object "%s"' % ob_name)
            out_strs.append('\no %s\n' % ob_name)

            for vco in extract_vertex_coords(depsgraph, ob):
                out_strs.append('v %.6f %.6f %.6f\n' % vco)

        return out_strs


    def execute(self, context):
        print(LOG_TAG, 'Exporting SLStudio obj model to file "%s"' % self.filepath)
        f = open(self.filepath, 'w', encoding='utf-8')
        for s in self.obj_gen(context):
            f.write(s)
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
            object['FixtureType'] = object.get('FixtureType') or ''
            object['ReverseOrder'] = 1 if object.get('ReverseOrder') else 0
            object.id_properties_ui('FixtureType').update(description='SLStudio: Fixture type')
            object.id_properties_ui('ReverseOrder').update(description='SLStudio: Reverse point order', min=0, max=1, soft_min=0, soft_max=1)
        return {'FINISHED'}

class SLStudioSetFixturePropertiesOperator(bpy.types.Operator):
    """SLStudio Set Fixture Properties Operator"""
    bl_idname = "object.slstudio_set_fixture_properties"
    bl_label = "Set Properties on Selected Fixtures"
    bl_options = {'REGISTER', 'UNDO'}

    FixtureType: bpy.props.StringProperty(name='Fixture Type', description="SLStudio: Fixture Type")
    ReverseOrder: bpy.props.BoolProperty(name='Reverse Order', description="SLStudio: Reverse Point Order")

    def invoke(self, context, event):
        wm = context.window_manager
        return wm.invoke_props_dialog(self)

    def execute(self, context):
        objects = filter_fixture_objects(context.selected_objects)

        bpy.ops.object.slstudio_setup_fixtures(all_or_selected='SELECTED')

        for object in objects:
            object['FixtureType'] = self.FixtureType
            object['ReverseOrder'] = self.ReverseOrder

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

        layout.operator(SLStudioSetupFixtureOperator.bl_idname, text='Setup All Fixtures').all_or_selected = 'ALL'
        layout.operator(SLStudioObjExporter.bl_idname, text='Export')

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
            row.prop(object, '["FixtureType"]', text='')
            row.prop(object, '["ReverseOrder"]', text='Flip?')
            col = row.column()
            props = col.operator(SelectObjectByNameOperator.bl_idname, text=('Deselect' if object.select_get() else 'Select'))
            props.object_name = object.name
            props.action = 'TOGGLE'
            props.apply_to_children = True
            col.enabled = object in context.selectable_objects
            #layout.prop(object, 'slstudio_props')

# Only needed if you want to add into a dynamic menu
def menu_func_export(self, context):
    self.layout.operator(SLStudioObjExporter.bl_idname, text="SLStudio Model Obj")


classes = [
    SLStudioFixtureProperties,
    SLStudioObjExporter,
    SelectObjectByNameOperator,
    SLStudioSelectAllFixturesOperator,
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
    bpy.utils.unregister_class(SLStudioSetupFixtureOperator)
    bpy.utils.unregister_class(SLStudioObjExporter)
    bpy.utils.unregister_class(SLStudioFixtureProperties)


if __name__ == "__main__":
    register()
