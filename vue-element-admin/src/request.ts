import axios, { AxiosRequestConfig } from "axios";
import { Message } from "element-ui";
import router from "./router";
interface RequestParam {
  [key: string]: any;
}
interface TokenVersion {
  tokenVersion?: number;
}
export interface Result {
  code: number;
  object: any;
  message: string;
}
export interface ErrorParam {
  alterMessage?: boolean;
  message?: string;
  otherMessage?: string;
  gotoLogin?: boolean;
  ignoreCode?: Array<number>;
  ignoreFalse?: boolean;
}
/**
 * token数据存储对象
 */
export const tokenData: {
  token: string | null;
  sessionId?: string;
  status: boolean;
  work: boolean;
  wait: boolean;
  version: number;
} = {
  token: null,
  status: true,
  work: false,
  wait: true,
  version: 1
};

const noTokenUrl = [];
// 全局打印异常
export const fetchErrorMessage = (
  error: Result,
  alterMessage: boolean = true, // 是否提示异常
  message?: string, // -1情况下的异常自定义提示
  otherMessage?: string, // 其他情况下的自定义提示
  ignoreCode?: Array<number> // 忽略处理的code列表
) => {
  if (ignoreCode && ignoreCode.includes(error.code)) {
    return;
  }
  if (alterMessage) {
    if ((message || otherMessage) && error.code === -1) {
      Message.error(message || otherMessage || error.message);
    } else {
      if (otherMessage) {
        Message.error(otherMessage);
      } else {
        if (error.code === 11000) {
          Message.error("您操作过于频繁，请稍后再试");
        } else {
          Message.error(error.message);
        }
      }
    }
  }
};
function errorCheck(
  config: AxiosRequestConfig & TokenVersion,
  data: Result,
  errorData: ErrorParam
): Promise<any> {
  if (data.code === 0) {
    // 在返回boolean值情况下 返回false认为失败
    if (errorData.ignoreFalse) {
      return Promise.resolve(data.object);
    }
    if (data.object !== false) {
      return Promise.resolve(data.object);
    }
    return Promise.reject(data);
  } else if (
    data.code === 10401 ||
    data.code === 10404 ||
    data.code === 10402
  ) {
    if (errorData.gotoLogin) {
      //  跳转登录页
      router.push("/login");
      return Promise.reject(data);
    } else {
      console.warn("token无效");
      return Promise.reject(data);
    }
  }
  fetchErrorMessage(
    data,
    errorData.alterMessage,
    errorData.message,
    errorData.otherMessage,
    errorData.ignoreCode
  );
  return Promise.reject(data);
}

export const request = async (
  config: AxiosRequestConfig,
  errorMsg: ErrorParam = {
    gotoLogin: true
  }
): Promise<any> => {
  if (errorMsg.gotoLogin !== false) {
    errorMsg.gotoLogin = true;
  }
  let tokenVersion = tokenData.version;
  let source = axios.CancelToken.source();
  try {
    return axios
      .request({
        baseURL: process.env.VUE_APP_BASE_API,
        cancelToken: source.token,
        ...config,
        headers: {
          ...config.headers,
          timestamp: new Date().getTime()
        }
      })
      .then(res => {
        return res.data;
      })
      .then((data: any) => {
        return errorCheck(
          {
            ...config,
            tokenVersion
          },
          <Result>data,
          errorMsg || {}
        );
      });
  } catch (error) {
    // 取消请求
    source.cancel();
    // 获取token失败 跳转登录页 只有在换取token中会失败
    return Promise.reject(error);
  }
};
/**
 * get请求
 * @param input url路径或者{@see AxiosRequestConfig}
 * @param param 请求参数 会自动序列话到url?key=value的形式
 */
export const get = (
  input: string | AxiosRequestConfig,
  param?: RequestParam,
  errorMsg?: ErrorParam
): Promise<any> => {
  let req: AxiosRequestConfig = {};
  if (typeof input === typeof "") {
    input = param ? input + "?" : input;
    if (param) {
      for (let key in param) {
        if (
          param[key] === undefined ||
          param[key] === null ||
          param[key] === ""
        ) {
          continue;
        }
        input += `${key}=${param[key]}&`;
      }
      input = (<string>input).substring(0, (<string>input).length - 1);
    }
    req.url = <string>input;
  } else {
    (<AxiosRequestConfig>input).url = param
      ? (<AxiosRequestConfig>input).url + "?"
      : (<AxiosRequestConfig>input).url;
    if (param) {
      for (let key in param) {
        (<AxiosRequestConfig>input).url += `${key}=${param[key]}&`;
      }
      (<AxiosRequestConfig>input).url = (<string>(
        (<AxiosRequestConfig>input).url
      )).substring(0, (<string>(<AxiosRequestConfig>input).url).length - 1);
    }
    req = <AxiosRequestConfig>input;
  }
  return request(
    {
      ...req,
      method: "GET"
    },
    errorMsg
  );
};

export const post = (
  input: string | AxiosRequestConfig,
  param?: RequestParam,
  errorMsg?: ErrorParam
): Promise<any> => {
  let req: AxiosRequestConfig = {};
  if (typeof input === typeof "") {
    req.url = <string>input;
  } else {
    req = <AxiosRequestConfig>input;
  }
  return request(
    {
      ...req,
      method: "POST",
      data: param
    },
    errorMsg
  );
};

export const postJson = (
  input: string | AxiosRequestConfig,
  param: RequestParam,
  errorMsg?: ErrorParam
): Promise<any> => {
  let req: AxiosRequestConfig = {};
  if (typeof input === typeof "") {
    req.url = <string>input;
  } else {
    req = <AxiosRequestConfig>input;
  }
  return request(
    {
      ...req,
      method: "POST",
      data: param,
      headers: {
        ...req.headers,
        "Content-Type": "application/json"
      }
    },
    errorMsg
  );
};

export const put = (
  input: string | AxiosRequestConfig,
  param?: RequestParam,
  errorMsg?: ErrorParam
): Promise<any> => {
  let req: AxiosRequestConfig = {};
  if (typeof input === typeof "") {
    req.url = <string>input;
  } else {
    req = <AxiosRequestConfig>input;
  }
  return request(
    {
      ...req,
      method: "PUT",
      data: param
    },
    errorMsg
  );
};

export const fetchDelete = (
  input: string | AxiosRequestConfig,
  param?: RequestParam,
  errorMsg?: ErrorParam
): Promise<any> => {
  let req: AxiosRequestConfig = {};
  if (typeof input === typeof "") {
    req.url = <string>input;
  } else {
    req = <AxiosRequestConfig>input;
  }
  return request(
    {
      ...req,
      method: "DELETE",
      data: param
    },
    errorMsg
  );
};
