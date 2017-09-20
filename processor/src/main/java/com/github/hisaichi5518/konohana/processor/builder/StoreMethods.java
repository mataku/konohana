package com.github.hisaichi5518.konohana.processor.builder;

import com.github.hisaichi5518.konohana.processor.definition.KeyDefinition;
import com.github.hisaichi5518.konohana.processor.definition.StoreDefinition;
import com.github.hisaichi5518.konohana.processor.types.AndroidTypes;
import com.github.hisaichi5518.konohana.processor.types.AnnotationTypes;
import com.github.hisaichi5518.konohana.processor.types.JavaTypes;
import com.github.hisaichi5518.konohana.processor.types.KonohanaTypes;
import com.github.hisaichi5518.konohana.processor.types.RxJavaTypes;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

public class StoreMethods {
    public static List<MethodSpec> build(StoreDefinition storeDefinition) {
        List<MethodSpec> specs = new ArrayList<>();

        specs.add(buildRemoveAllSpec());

        storeDefinition.keyDefinitionStream().forEach(keyDefinition -> {
            specs.add(buildGetterSpec(storeDefinition, keyDefinition));
            specs.add(buildSetterSpec(keyDefinition));
            specs.add(buildContainsSpec(keyDefinition));
            specs.add(buildRemoverSpec(keyDefinition));
        });

        specs.add(buildKeyChangesSpec());

        return specs;
    }

    private static MethodSpec buildRemoveAllSpec() {
        return MethodSpec.methodBuilder("removeAll")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("prefs.edit().clear().apply()")
                .build();
    }

    private static MethodSpec buildKeyChangesSpec() {
        return MethodSpec.methodBuilder("keyChanges")
                .addModifiers(Modifier.PRIVATE)
                .addAnnotation(AnnotationTypes.NonNull)
                .returns(ParameterizedTypeName.get(RxJavaTypes.Observable, JavaTypes.String))
                .beginControlFlow("return $T.create(new $T<$T>()", RxJavaTypes.Observable, RxJavaTypes.ObservableOnSubscribe, JavaTypes.String)
                .addCode("@$T\n", AnnotationTypes.Override)
                .beginControlFlow("public void subscribe(final $T<$T> emitter) throws $T", RxJavaTypes.ObservableEmitter, JavaTypes.String, Exception.class)
                .beginControlFlow("final $T listener = new $T()", AndroidTypes.OnSharedPreferenceChangeListener, AndroidTypes.OnSharedPreferenceChangeListener)
                .addCode("@$T\n", AnnotationTypes.Override)
                .beginControlFlow("public void onSharedPreferenceChanged($T preferences, $T key)", AndroidTypes.SharedPreferences, JavaTypes.String)
                .addStatement("emitter.onNext(key)")
                .endControlFlow()
                .endControlFlow("")
                .beginControlFlow("emitter.setCancellable(new $T()", RxJavaTypes.Cancellable)
                .addCode("@$T\n", AnnotationTypes.Override)
                .beginControlFlow("public void cancel() throws $T", JavaTypes.Exception)
                .addStatement("prefs.unregisterOnSharedPreferenceChangeListener(listener)")
                .endControlFlow()
                .endControlFlow(")")
                .addStatement("prefs.registerOnSharedPreferenceChangeListener(listener)")
                .endControlFlow()
                .endControlFlow(")")
                .build();
    }

    private static MethodSpec buildGetterSpec(StoreDefinition storeDefinition, KeyDefinition keyDefinition) {
        return MethodSpec.methodBuilder(keyDefinition.getGetterName())
                .returns(keyDefinition.getFieldTypeName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationTypes.NonNull)
                .addStatement("return $T.get(prefs, $S, $T.$L)",
                        /* TODO */ KonohanaTypes.StringPrefsAdapter, keyDefinition.getPrefsKeyName(), storeDefinition.getInterfaceName(), keyDefinition.getFieldName())
                .build();
    }

    private static MethodSpec buildSetterSpec(KeyDefinition keyDefinition) {
        return MethodSpec.methodBuilder(keyDefinition.getSetterName())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(keyDefinition.getFieldTypeName(), "value").addAnnotation(AnnotationTypes.NonNull).build())
                .addStatement("$T.set(prefs, $S, value)",
                        /* TODO */ KonohanaTypes.StringPrefsAdapter, keyDefinition.getPrefsKeyName())
                .build();

    }

    private static MethodSpec buildContainsSpec(KeyDefinition keyDefinition) {
        return MethodSpec.methodBuilder(keyDefinition.getContainsName())
                .returns(boolean.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return prefs.contains($S)", keyDefinition.getPrefsKeyName())
                .build();
    }

    private static MethodSpec buildRemoverSpec(KeyDefinition keyDefinition) {
        return MethodSpec.methodBuilder(keyDefinition.getRemoverName())
                .addModifiers(Modifier.PUBLIC)
                .addStatement("prefs.edit().remove($S).apply()", keyDefinition.getPrefsKeyName())
                .build();
    }
}
