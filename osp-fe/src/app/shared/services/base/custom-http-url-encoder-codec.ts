import { HttpUrlEncodingCodec } from "@angular/common/http";

export class CustomHttpUrlEncoderCodec extends HttpUrlEncodingCodec {
    encodeKey(key: string): string {
        key = super.encodeKey(key);
        return key.replace(/\+/gi, '%2B');
    }
    encodeValue(value: string): string {
        value = super.encodeValue(value);
        return value.replace(/\+/gi, '%2B');
    }
}
