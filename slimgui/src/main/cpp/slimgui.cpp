#include <GLFW/glfw3.h>
#include <iostream>

#include "com_symmetrylabs_slstudio_ui_gdx_UI.h"
#include "handle.hpp"
#include "imgui.h"
#include "imgui_impl_opengl2.h"
#include "imgui_impl_glfw.h"

#define MAX_INPUT_LENGTH 511

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_init(JNIEnv *env, jclass cls, jlong windowHandle) {
	jfieldID fid;

	fid = env->GetStaticFieldID(cls, "TREE_FLAG_LEAF", "I");
	env->SetStaticIntField(cls, fid, ImGuiTreeNodeFlags_Leaf);
	fid = env->GetStaticFieldID(cls, "TREE_FLAG_DEFAULT_OPEN", "I");
	env->SetStaticIntField(cls, fid, ImGuiTreeNodeFlags_DefaultOpen);
	fid = env->GetStaticFieldID(cls, "TREE_FLAG_SELECTED", "I");
	env->SetStaticIntField(cls, fid, ImGuiTreeNodeFlags_Selected);
	fid = env->GetStaticFieldID(cls, "WINDOW_HORIZ_SCROLL", "I");
	env->SetStaticIntField(cls, fid, ImGuiWindowFlags_HorizontalScrollbar);

	glfwInit();
	GLFWwindow* window = reinterpret_cast<GLFWwindow*>(windowHandle);
	ImGui::CreateContext();
	bool ok = ImGui_ImplGlfw_InitForOpenGL(window, false);
	if (!ok) {
		std::cout << "failed to init glfw" << std::endl;
		return 0;
	}
	ok = ImGui_ImplOpenGL2_Init();
	if (!ok) {
		std::cout << "failed to init opengl2" << std::endl;
		return 0;
	}

	ImVec4* colors = ImGui::GetStyle().Colors;
	colors[ImGuiCol_Text]                   = ImVec4(1.00f, 1.00f, 1.00f, 1.00f);
	colors[ImGuiCol_TextDisabled]           = ImVec4(0.50f, 0.50f, 0.50f, 1.00f);
	colors[ImGuiCol_WindowBg]               = ImVec4(0.06f, 0.06f, 0.06f, 0.94f);
	colors[ImGuiCol_ChildBg]                = ImVec4(1.00f, 1.00f, 1.00f, 0.00f);
	colors[ImGuiCol_PopupBg]                = ImVec4(0.08f, 0.08f, 0.08f, 0.94f);
	colors[ImGuiCol_Border]                 = ImVec4(0.43f, 0.43f, 0.50f, 0.50f);
	colors[ImGuiCol_BorderShadow]           = ImVec4(0.00f, 0.00f, 0.00f, 0.00f);
	colors[ImGuiCol_FrameBg]                = ImVec4(0.20f, 0.21f, 0.22f, 0.54f);
	colors[ImGuiCol_FrameBgHovered]         = ImVec4(0.40f, 0.40f, 0.40f, 0.40f);
	colors[ImGuiCol_FrameBgActive]          = ImVec4(0.18f, 0.18f, 0.18f, 0.67f);
	colors[ImGuiCol_TitleBg]                = ImVec4(0.04f, 0.04f, 0.04f, 1.00f);
	colors[ImGuiCol_TitleBgActive]          = ImVec4(0.29f, 0.29f, 0.29f, 1.00f);
	colors[ImGuiCol_TitleBgCollapsed]       = ImVec4(0.00f, 0.00f, 0.00f, 0.51f);
	colors[ImGuiCol_MenuBarBg]              = ImVec4(0.14f, 0.14f, 0.14f, 1.00f);
	colors[ImGuiCol_ScrollbarBg]            = ImVec4(0.02f, 0.02f, 0.02f, 0.53f);
	colors[ImGuiCol_ScrollbarGrab]          = ImVec4(0.31f, 0.31f, 0.31f, 1.00f);
	colors[ImGuiCol_ScrollbarGrabHovered]   = ImVec4(0.41f, 0.41f, 0.41f, 1.00f);
	colors[ImGuiCol_ScrollbarGrabActive]    = ImVec4(0.51f, 0.51f, 0.51f, 1.00f);
	colors[ImGuiCol_CheckMark]              = ImVec4(0.94f, 0.94f, 0.94f, 1.00f);
	colors[ImGuiCol_SliderGrab]             = ImVec4(0.51f, 0.51f, 0.51f, 1.00f);
	colors[ImGuiCol_SliderGrabActive]       = ImVec4(0.86f, 0.86f, 0.86f, 1.00f);
	colors[ImGuiCol_Button]                 = ImVec4(0.44f, 0.44f, 0.44f, 0.40f);
	colors[ImGuiCol_ButtonHovered]          = ImVec4(0.46f, 0.47f, 0.48f, 1.00f);
	colors[ImGuiCol_ButtonActive]           = ImVec4(0.42f, 0.42f, 0.42f, 1.00f);
	colors[ImGuiCol_Header]                 = ImVec4(0.70f, 0.70f, 0.70f, 0.31f);
	colors[ImGuiCol_HeaderHovered]          = ImVec4(0.70f, 0.70f, 0.70f, 0.80f);
	colors[ImGuiCol_HeaderActive]           = ImVec4(0.48f, 0.50f, 0.52f, 1.00f);
	colors[ImGuiCol_Separator]              = ImVec4(0.43f, 0.43f, 0.50f, 0.50f);
	colors[ImGuiCol_SeparatorHovered]       = ImVec4(0.72f, 0.72f, 0.72f, 0.78f);
	colors[ImGuiCol_SeparatorActive]        = ImVec4(0.51f, 0.51f, 0.51f, 1.00f);
	colors[ImGuiCol_ResizeGrip]             = ImVec4(0.91f, 0.91f, 0.91f, 0.25f);
	colors[ImGuiCol_ResizeGripHovered]      = ImVec4(0.81f, 0.81f, 0.81f, 0.67f);
	colors[ImGuiCol_ResizeGripActive]       = ImVec4(0.46f, 0.46f, 0.46f, 0.95f);
	colors[ImGuiCol_PlotLines]              = ImVec4(0.61f, 0.61f, 0.61f, 1.00f);
	colors[ImGuiCol_PlotLinesHovered]       = ImVec4(1.00f, 0.43f, 0.35f, 1.00f);
	colors[ImGuiCol_PlotHistogram]          = ImVec4(0.73f, 0.60f, 0.15f, 1.00f);
	colors[ImGuiCol_PlotHistogramHovered]   = ImVec4(1.00f, 0.60f, 0.00f, 1.00f);
	colors[ImGuiCol_TextSelectedBg]         = ImVec4(0.87f, 0.87f, 0.87f, 0.35f);
	colors[ImGuiCol_ModalWindowDarkening]   = ImVec4(0.80f, 0.80f, 0.80f, 0.35f);
	colors[ImGuiCol_DragDropTarget]         = ImVec4(1.00f, 1.00f, 0.00f, 0.90f);
	colors[ImGuiCol_NavHighlight]           = ImVec4(0.60f, 0.60f, 0.60f, 1.00f);
	colors[ImGuiCol_NavWindowingHighlight]  = ImVec4(1.00f, 1.00f, 1.00f, 0.70f);

	ImGuiIO &io = ImGui::GetIO();
	io.ConfigFlags |= ImGuiConfigFlags_DockingEnable;

	std::cout << "successfully initialized" << std::endl;
	return 1;
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_newFrame(JNIEnv *, jclass) {
	ImGui_ImplOpenGL2_NewFrame();
	ImGui_ImplGlfw_NewFrame();
	ImGui::NewFrame();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_render(JNIEnv *, jclass) {
	ImGui::Render();
	ImGui_ImplOpenGL2_RenderDrawData(ImGui::GetDrawData());
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_shutdown(JNIEnv *, jclass) {
	ImGui_ImplOpenGL2_Shutdown();
	ImGui_ImplGlfw_Shutdown();
	ImGui::DestroyContext();
	return 1;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_setNextWindowDefaults(
	JNIEnv *env, jclass, jint x, jint y, jint w, jint h) {
	ImGui::SetNextWindowSize(ImVec2(w, h), ImGuiCond_FirstUseEver);
	ImGui::SetNextWindowPos(ImVec2(x, y), ImGuiCond_FirstUseEver);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_setNextWindowDefaultToCursor(
	JNIEnv *env, jclass, jint w, jint h) {
	ImGui::SetNextWindowSize(ImVec2(w, h), ImGuiCond_FirstUseEver);
	ImGui::SetNextWindowPos(ImGui::GetCursorScreenPos(), ImGuiCond_FirstUseEver);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_setNextWindowContentSize(JNIEnv *, jclass, jint w, jint h) {
	ImGui::SetNextWindowContentSize(ImVec2(w, h));
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_begin(JNIEnv *env, jclass, jstring jstr) {
	const char *str = env->GetStringUTFChars(jstr, 0);
	ImGui::Begin(str);
	env->ReleaseStringUTFChars(jstr, str);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_beginDocked(JNIEnv *env, jclass, jstring jstr) {
	const char *str = env->GetStringUTFChars(jstr, 0);
	ImGuiIO &io = ImGui::GetIO();
	ImGui::SetNextWindowPos(ImVec2(0, io.DisplaySize.y), ImGuiCond_Always, ImVec2(1, 1));
	ImGui::SetNextWindowSize(ImVec2(io.DisplaySize.x, 300));
	int flags = ImGuiWindowFlags_NoMove |
		ImGuiWindowFlags_NoTitleBar |
		ImGuiWindowFlags_NoResize |
		ImGuiWindowFlags_AlwaysAutoResize |
		ImGuiWindowFlags_NoSavedSettings |
		ImGuiWindowFlags_NoFocusOnAppearing |
		ImGuiWindowFlags_NoNav;
	ImGui::Begin(str, NULL, flags);
	env->ReleaseStringUTFChars(jstr, str);
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_beginClosable(JNIEnv *env, jclass, jstring jstr) {
	const char *str = env->GetStringUTFChars(jstr, 0);
	bool isOpen = true;
	ImGui::Begin(str, &isOpen);
	env->ReleaseStringUTFChars(jstr, str);
	return isOpen ? 1 : 0;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_end(JNIEnv *, jclass) {
	ImGui::End();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_sameLine(JNIEnv *, jclass) {
	ImGui::SameLine();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_columnsStart(JNIEnv *env, jclass, jint num, jstring jlabel) {
	const char *label = env->GetStringUTFChars(jlabel, 0);
	ImGui::Columns(num, label);
	env->ReleaseStringUTFChars(jlabel, label);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_nextColumn(JNIEnv *, jclass) {
	ImGui::NextColumn();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_columnsEnd(JNIEnv *, jclass) {
	ImGui::Columns(1);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_separator(JNIEnv *, jclass) {
	ImGui::Separator();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_spacing(JNIEnv *, jclass) {
	ImGui::Dummy(ImVec2(5, 5));
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_beginChild(
	JNIEnv *env, jclass, jstring jid, jboolean border, jint flags) {
	const char *id = env->GetStringUTFChars(jid, 0);
	bool res = ImGui::BeginChild(id, ImVec2(0, 0), border == 1, flags);
	env->ReleaseStringUTFChars(jid, id);
	return res ? 1 : 0;

}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_endChild(JNIEnv *, jclass) {
	ImGui::EndChild();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_beginGroup(JNIEnv *, jclass) {
	ImGui::BeginGroup();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_endGroup(JNIEnv *, jclass) {
	ImGui::EndGroup();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_text(JNIEnv *env, jclass, jstring jstr) {
	const char *str = env->GetStringUTFChars(jstr, 0);
	ImGui::Text(str);
	env->ReleaseStringUTFChars(jstr, str);
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_button(JNIEnv *env, jclass, jstring jstr) {
	const char *str = env->GetStringUTFChars(jstr, 0);
	bool res = ImGui::Button(str);
	env->ReleaseStringUTFChars(jstr, str);
	return res ? 1 : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_checkbox(
	JNIEnv *env, jclass, jstring jlabel, jboolean v) {
	const char *label = env->GetStringUTFChars(jlabel, 0);
	bool res = v == 1 ? true : false;
	ImGui::Checkbox(label, &res);
	env->ReleaseStringUTFChars(jlabel, label);
	return res ? 1 : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_selectable(
	JNIEnv *env, jclass, jstring jlabel, jboolean v) {
	const char *label = env->GetStringUTFChars(jlabel, 0);
	bool res = v == 1 ? true : false;
	ImGui::Selectable(label, &res);
	env->ReleaseStringUTFChars(jlabel, label);
	return res ? 1 : 0;
}

JNIEXPORT jstring JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_inputText(
	JNIEnv *env, jclass, jstring jlabel, jstring jstr) {
	const char *label = env->GetStringUTFChars(jlabel, 0);
	const char *str = env->GetStringUTFChars(jstr, 0);
	char input_buf[MAX_INPUT_LENGTH + 1] = {0};
	strncpy(input_buf, str, MAX_INPUT_LENGTH);
	ImGui::InputText(label, input_buf, MAX_INPUT_LENGTH + 1);
	env->ReleaseStringUTFChars(jstr, str);
	env->ReleaseStringUTFChars(jlabel, label);
	return env->NewStringUTF(input_buf);
}

JNIEXPORT jint JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_colorPicker(
	JNIEnv *env, jclass, jstring jlabel, jint jcolor) {
	const char *label = env->GetStringUTFChars(jlabel, 0);

	unsigned int c = static_cast<unsigned int>(jcolor);
	float color[4] {
		(float)((c >> 16) & 0xFF) / 255,
		(float)((c >>  8) & 0xFF) / 255,
		(float)((c      ) & 0xFF) / 255,
		255.f};
	ImGui::ColorEdit3(label, color, ImGuiColorEditFlags_HSV | ImGuiColorEditFlags_Float);
	unsigned int res =
		0xFF000000 |
		(0xFF & (int)(color[0] * 255)) << 16 |
		(0xFF & (int)(color[1] * 255)) <<  8 |
		(0xFF & (int)(color[2] * 255));
	env->ReleaseStringUTFChars(jlabel, label);
	return static_cast<jint>(res);
}

JNIEXPORT jfloat JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_sliderFloat(
	JNIEnv * env, jclass, jstring jlabel, jfloat v, jfloat v0, jfloat v1, jboolean vert) {
	const char *label = env->GetStringUTFChars(jlabel, 0);
	jfloat res = v;
	if (vert) {
		ImGui::VSliderFloat(label, ImVec2(20, 200), &res, v0, v1);
	} else {
		ImGui::SliderFloat(label, &res, v0, v1);
	}
	env->ReleaseStringUTFChars(jlabel, label);
	return res;
}

JNIEXPORT jint JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_sliderInt(
	JNIEnv *env, jclass, jstring jlabel, jint v, jint v0, jint v1) {
	const char *label = env->GetStringUTFChars(jlabel, 0);
	jint res = v;
	ImGui::SliderInt(label, &res, v0, v1);
	env->ReleaseStringUTFChars(jlabel, label);
	return res;
}

JNIEXPORT jint JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_combo(
	JNIEnv *env, jclass, jstring jlabel, jint selected, jobjectArray joptions) {
	jsize optionsLen = env->GetArrayLength(joptions);
	const char **options = new const char*[optionsLen];
	for (int i = 0; i < optionsLen; i++) {
		jstring joption = (jstring) env->GetObjectArrayElement(joptions, i);
		options[i] = env->GetStringUTFChars(joption, 0);
	}
	const char *label = env->GetStringUTFChars(jlabel, 0);

	jint res = selected;
	ImGui::Combo(label, &res, options, optionsLen);

	for (int i = 0; i < optionsLen; i++) {
		jstring joption = (jstring) env->GetObjectArrayElement(joptions, i);
		env->ReleaseStringUTFChars(joption, options[i]);
	}
	env->ReleaseStringUTFChars(jlabel, label);
	delete[] options;

	return res;
}

JNIEXPORT jfloat JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_floatBox(JNIEnv *env, jclass, jstring jlabel, jfloat v) {
	const char *label = env->GetStringUTFChars(jlabel, 0);
	jfloat res = v;
	ImGui::DragFloat(label, &res);
	env->ReleaseStringUTFChars(jlabel, label);
	return res;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_beginMainMenuBar(JNIEnv *env, jclass) {
	return ImGui::BeginMainMenuBar() ? 1 : 0;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_endMainMenuBar(JNIEnv *, jclass) {
	return ImGui::EndMainMenuBar();
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_beginMenu(JNIEnv *env, jclass, jstring jlabel) {
	const char *label = env->GetStringUTFChars(jlabel, 0);
	bool res = ImGui::BeginMenu(label);
	env->ReleaseStringUTFChars(jlabel, label);
	return res ? 1 : 0;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_endMenu(JNIEnv *, jclass) {
	ImGui::EndMenu();
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_menuItem(
	JNIEnv *env, jclass, jstring jlabel, jstring jshortcut,
	jboolean selected, jboolean enabled) {
	const char *shortcut =
		jshortcut == NULL ? NULL : env->GetStringUTFChars(jshortcut, 0);
	const char *label = env->GetStringUTFChars(jlabel, 0);

	bool res = ImGui::MenuItem(label, shortcut, selected == 1, enabled == 1);

	if (shortcut != NULL) {
		env->ReleaseStringUTFChars(jshortcut, shortcut);
	}
	env->ReleaseStringUTFChars(jlabel, label);
	return res ? 1 : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_treeNode(
	JNIEnv *env, jclass, jstring jid, jint flags, jstring jlabel) {
	const char *id = env->GetStringUTFChars(jid, 0);
	const char *label = env->GetStringUTFChars(jlabel, 0);
	bool res = ImGui::TreeNodeEx(id, flags, label);
	env->ReleaseStringUTFChars(jid, id);
	env->ReleaseStringUTFChars(jlabel, label);
	return res ? 1 : 0;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_treePop(JNIEnv *, jclass) {
	ImGui::TreePop();
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_isItemClicked(JNIEnv *, jclass, jint button) {
	return ImGui::IsItemClicked(button) ? 1 : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_isItemActive(JNIEnv *, jclass) {
	return ImGui::IsItemActive() ? 1 : 0;
}

JNIEXPORT jfloat JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_getFrameRate(JNIEnv *, jclass) {
	return ImGui::GetIO().Framerate;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_wantCaptureKeyboard(JNIEnv *, jclass) {
	return ImGui::GetIO().WantCaptureKeyboard;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_wantCaptureMouse(JNIEnv *, jclass) {
	return ImGui::GetIO().WantCaptureMouse;
}

void update_modifiers() {
	ImGuiIO &io = ImGui::GetIO();
	io.KeyCtrl = io.KeysDown[GLFW_KEY_LEFT_CONTROL] || io.KeysDown[GLFW_KEY_RIGHT_CONTROL];
	io.KeyShift = io.KeysDown[GLFW_KEY_LEFT_SHIFT] || io.KeysDown[GLFW_KEY_RIGHT_SHIFT];
	io.KeyAlt = io.KeysDown[GLFW_KEY_LEFT_ALT] || io.KeysDown[GLFW_KEY_RIGHT_ALT];
	io.KeySuper = io.KeysDown[GLFW_KEY_LEFT_SUPER] || io.KeysDown[GLFW_KEY_RIGHT_SUPER];
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_keyDown(JNIEnv *, jclass, jint keycode) {
	ImGui::GetIO().KeysDown[keycode] = true;
	update_modifiers();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_keyUp(JNIEnv *, jclass, jint keycode) {
	ImGui::GetIO().KeysDown[keycode] = false;
	update_modifiers();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_addInputCharacter(JNIEnv *, jclass, jchar c) {
	ImGui::GetIO().AddInputCharacter((ImWchar) c);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_scrolled(JNIEnv *, jclass, jfloat amount) {
	ImGui::GetIO().MouseWheel -= amount;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_showDemoWindow(JNIEnv *, jclass) {
	ImGui::ShowDemoWindow();
}
