# Needs to be manually reloaded in blender after changing

import os
import re
import datetime
import bpy

# ExportHelper is a helper class, defines filename and
# invoke() function which calls the file selector.
from bpy_extras.io_utils import ExportHelper
from bpy.props import StringProperty
from bpy.types import Operator

LOG_TAG = '[SLStudioObjExporter]'
OBJECT_NAME_REGEXP = re.compile(r'^(Fixture|Shape)(_\w+)?\.(\d+)$')
OUTPUT_NAME_REGEXP = re.compile(r'^(?:Controller\.(\d+)_)?Output\.(\d+)$')

def extract_vertex_coords(depsgraph, ob_main):
    vcos = []

    obs = [(ob_main, ob_main.matrix_world)]
    if ob_main.is_instancer:
        obs += [(dup.instance_object.original, dup.matrix_world.copy())
                    for dup in depsgraph.object_instances
                    if dup.parent and dup.parent.original == ob_main]

    for ob, ob_matrix in obs:
        if ob.type == 'EMPTY':
            for child in ob.children:
                #if child.name.startswith(ob.name):
                if child.type == 'MESH':
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

                mesh.transform(ob_matrix)

                for v in mesh.vertices:
                    vcos.append(v.co[:])
            finally:
                # clean up
                ob_for_convert.to_mesh_clear()
    return vcos

class SLStudioObjExporter(Operator, ExportHelper):
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


# Only needed if you want to add into a dynamic menu
def menu_func_export(self, context):
    self.layout.operator(SLStudioObjExporter.bl_idname, text="SLStudio Model Obj")


# Register and add to the "file selector" menu (required to use F3 search "Text Export Operator" for quick access).
def register():
    bpy.utils.register_class(SLStudioObjExporter)
    bpy.types.TOPBAR_MT_file_export.append(menu_func_export)


def unregister():
    bpy.utils.unregister_class(SLStudioObjExporter)
    bpy.types.TOPBAR_MT_file_export.remove(menu_func_export)


if __name__ == "__main__":
    register()
