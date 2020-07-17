package net.okocraft.box.api.util;

/**
 * 何らかの実行結果を示すインターフェース。
 * <p>
 * これは、{@link Result#isSuccess()} を関数メソッドに持つ関数型インターフェースである。
 */
@FunctionalInterface
public interface Result {

    Result SUCCESS = () -> true;
    Result FAILURE = () -> false;

    /**
     * 処理の結果として返された {@link Result} が成功であることを示しているか。
     *
     * @return 成功であれば {@link true}, そうでなければ {@link false}
     */
    boolean isSuccess();
}
