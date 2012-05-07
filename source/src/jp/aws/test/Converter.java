package jp.aws.test;

/**
 * Spinnerに表示するテキストのConvertを行います。
 */
public interface Converter<T> {

	/**
	 * Spinnerに表示したいテキストを返却するように実装してください。
	 */
	public String toDisplayString(T t);

}
