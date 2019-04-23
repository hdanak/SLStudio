/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_symmetrylabs_slstudio_ui_v2_UI */

#ifndef _Included_com_symmetrylabs_slstudio_ui_v2_UI
#define _Included_com_symmetrylabs_slstudio_ui_v2_UI
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    init
 * Signature: (JZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_init
  (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    newFrame
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_newFrame
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    render
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_render
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    shutdown
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_shutdown
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    addFont
 * Signature: (Ljava/lang/String;Ljava/nio/ByteBuffer;F)J
 */
JNIEXPORT jlong JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_addFont
  (JNIEnv *, jclass, jstring, jobject, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    pushFont
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_pushFont
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    popFont
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_popFont
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    pushColor
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_pushColor
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    popColor
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_popColor
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    pushWidth
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_pushWidth
  (JNIEnv *, jclass, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    popWidth
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_popWidth
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    setNextWindowPosition
 * Signature: (FFFF)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowPosition
  (JNIEnv *, jclass, jfloat, jfloat, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    setNextWindowDefaults
 * Signature: (FFFF)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowDefaults
  (JNIEnv *, jclass, jfloat, jfloat, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    setNextWindowDefaultToCursor
 * Signature: (FF)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowDefaultToCursor
  (JNIEnv *, jclass, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    setNextWindowContentSize
 * Signature: (FF)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowContentSize
  (JNIEnv *, jclass, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    setNextWindowSize
 * Signature: (FF)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowSize
  (JNIEnv *, jclass, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    getContentRegionSize
 * Signature: ()Lcom/symmetrylabs/slstudio/ui/v2/UI/Size;
 */
JNIEXPORT jobject JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_getContentRegionSize
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    begin
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_begin
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    beginClosable
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginClosable
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    end
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_end
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    sameLine
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_sameLine
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    beginTable
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginTable
  (JNIEnv *, jclass, jint, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    nextCell
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_nextCell
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    endTable
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endTable
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    separator
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_separator
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    spacing
 * Signature: (FF)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_spacing
  (JNIEnv *, jclass, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    beginChild
 * Signature: (Ljava/lang/String;ZIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginChild
  (JNIEnv *, jclass, jstring, jboolean, jint, jint, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    endChild
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endChild
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    beginGroup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginGroup
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    endGroup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endGroup
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    beginPopup
 * Signature: (Ljava/lang/String;ZI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginPopup
  (JNIEnv *, jclass, jstring, jboolean, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    endPopup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endPopup
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    openPopup
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_openPopup
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    closePopup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_closePopup
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    text
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_text
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    labelText
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_labelText
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    button
 * Signature: (Ljava/lang/String;FF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_button
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    checkbox
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_checkbox
  (JNIEnv *, jclass, jstring, jboolean);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    selectable
 * Signature: (Ljava/lang/String;ZF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_selectable
  (JNIEnv *, jclass, jstring, jboolean, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    inputText
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_inputText
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    colorPicker
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_colorPicker
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    colorPickerHSV
 * Signature: (Ljava/lang/String;FFF)[F
 */
JNIEXPORT jfloatArray JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_colorPickerHSV
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    sliderFloat
 * Signature: (Ljava/lang/String;FFF)F
 */
JNIEXPORT jfloat JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_sliderFloat
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    vertSliderFloat
 * Signature: (Ljava/lang/String;FFFLjava/lang/String;FF)F
 */
JNIEXPORT jfloat JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_vertSliderFloat
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jfloat, jstring, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    sliderInt
 * Signature: (Ljava/lang/String;III)I
 */
JNIEXPORT jint JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_sliderInt
  (JNIEnv *, jclass, jstring, jint, jint, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    combo
 * Signature: (Ljava/lang/String;I[Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_combo
  (JNIEnv *, jclass, jstring, jint, jobjectArray);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    floatBox
 * Signature: (Ljava/lang/String;FFFFLjava/lang/String;)F
 */
JNIEXPORT jfloat JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_floatBox
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jfloat, jfloat, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    knobFloat
 * Signature: (Ljava/lang/String;FF)F
 */
JNIEXPORT jfloat JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_knobFloat
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    knobModulatedFloat
 * Signature: (Ljava/lang/String;FFFI[F[F[I)F
 */
JNIEXPORT jfloat JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_knobModulatedFloat
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jfloat, jint, jfloatArray, jfloatArray, jintArray);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    collapsibleSection
 * Signature: (Ljava/lang/String;ZI)Lcom/symmetrylabs/slstudio/ui/v2/UI/CollapseResult;
 */
JNIEXPORT jobject JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_collapsibleSection
  (JNIEnv *, jclass, jstring, jboolean, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    histogram
 * Signature: (Ljava/lang/String;[FFFI)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_histogram
  (JNIEnv *, jclass, jstring, jfloatArray, jfloat, jfloat, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    plot
 * Signature: (Ljava/lang/String;[FFFI)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_plot
  (JNIEnv *, jclass, jstring, jfloatArray, jfloat, jfloat, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    colorButton
 * Signature: (Ljava/lang/String;FFF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_colorButton
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    envelopeEditor
 * Signature: (Ljava/lang/String;[D[D[D)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_envelopeEditor
  (JNIEnv *, jclass, jstring, jdoubleArray, jdoubleArray, jdoubleArray);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    image
 * Signature: (IFFFFFF)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_image
  (JNIEnv *, jclass, jint, jfloat, jfloat, jfloat, jfloat, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    imageButton
 * Signature: (IFFFFFF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_imageButton
  (JNIEnv *, jclass, jint, jfloat, jfloat, jfloat, jfloat, jfloat, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    beginMainMenuBar
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginMainMenuBar
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    endMainMenuBar
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endMainMenuBar
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    beginMenu
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginMenu
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    endMenu
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endMenu
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    menuItem
 * Signature: (Ljava/lang/String;Ljava/lang/String;ZZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_menuItem
  (JNIEnv *, jclass, jstring, jstring, jboolean, jboolean);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    menuItemToggle
 * Signature: (Ljava/lang/String;Ljava/lang/String;ZZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_menuItemToggle
  (JNIEnv *, jclass, jstring, jstring, jboolean, jboolean);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    beginContextMenu
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginContextMenu
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    contextMenuItem
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_contextMenuItem
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    endContextMenu
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endContextMenu
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    treeNode
 * Signature: (Ljava/lang/String;ILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_treeNode
  (JNIEnv *, jclass, jstring, jint, jstring);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    treePop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_treePop
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    setNextTreeNodeOpen
 * Signature: (ZI)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextTreeNodeOpen
  (JNIEnv *, jclass, jboolean, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    beginDragDropSource
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginDragDropSource
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    endDragDropSource
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endDragDropSource
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    setDragDropPayload
 * Signature: (Ljava/lang/String;Ljava/lang/Object;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_setDragDropPayload
  (JNIEnv *, jclass, jstring, jobject);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    beginDragDropTarget
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginDragDropTarget
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    endDragDropTarget
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endDragDropTarget
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    acceptDragDropPayload
 * Signature: (Ljava/lang/String;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_acceptDragDropPayload
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    isItemClicked
 * Signature: (IZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_isItemClicked
  (JNIEnv *, jclass, jint, jboolean);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    isItemDoubleClicked
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_isItemDoubleClicked
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    isItemActive
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_isItemActive
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    isAltDown
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_isAltDown
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    isCtrlDown
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_isCtrlDown
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    isShiftDown
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_isShiftDown
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    getFrameRate
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_getFrameRate
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    wantCaptureKeyboard
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_wantCaptureKeyboard
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    wantCaptureMouse
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_wantCaptureMouse
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    keyDown
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_keyDown
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    keyUp
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_keyUp
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    addInputCharacter
 * Signature: (C)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_addInputCharacter
  (JNIEnv *, jclass, jchar);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    scrolled
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_scrolled
  (JNIEnv *, jclass, jfloat);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    showDemoWindow
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_showDemoWindow
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    showMetricsWindow
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_showMetricsWindow
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    showStyleEditor
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_showStyleEditor
  (JNIEnv *, jclass);

/*
 * Class:     com_symmetrylabs_slstudio_ui_v2_UI
 * Method:    showAboutWindow
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_showAboutWindow
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
