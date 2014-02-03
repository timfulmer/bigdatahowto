package info.bigdatahowto.defaults.js;

/**
 * Interface defining templates used by JavaScriptProcessor to interface with
 * JavaScript behavior functions.
 *
 * @author timfulmer
 */
public interface JavaScriptProcessorTemplate {
    String processWithMeta();
    String processWithoutMeta();
    String errorWithMeta();
    String errorWithoutMeta();
}
